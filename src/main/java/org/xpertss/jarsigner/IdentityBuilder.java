/*
 * Copyright 2025 XpertSoftware
 *
 * Created By: cfloersch
 * Date: 1/17/2025
 */
package org.xpertss.jarsigner;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class IdentityBuilder {

   private static final Path NONE = Paths.get("NONE");


   private String alias;
   private String storeType;

   private Path trustStore;
   private Path keyStore;

   private Provider provider;

   private Path certPath;

   private boolean strict;

   private KeyStore.PasswordProtection keyPass;
   private KeyStore.PasswordProtection storePass;


   public IdentityBuilder strict(boolean strict)
   {
      this.strict = strict;
      return this;
   }

   public IdentityBuilder alias(String alias)
   {
      this.alias = alias;
      return this;
   }


   public IdentityBuilder trustStore(Path trustStore)
      throws NoSuchFileException
   {
      this.trustStore = validate(trustStore, "Truststore");
      return this;
   }


   public IdentityBuilder keyStore(Path keyStore)
      throws NoSuchFileException
   {
      if(keyStore != null && !keyStore.equals(NONE) && (!Files.exists(keyStore) || !Files.isReadable(keyStore))) {
         throw new NoSuchFileException(String.format("Keystore %s not found", keyStore));
      }
      this.keyStore = keyStore;
      return this;
   }


   public IdentityBuilder storeType(String storeType)
   {
      this.storeType = storeType;
      return this;
   }

   public IdentityBuilder storeType(String storeType, String providerName)
      throws NoSuchProviderException
   {
      if((this.provider = Security.getProvider(providerName)) == null) {
         throw new NoSuchProviderException(String.format("No Provider %s found", providerName));
      }
      this.storeType = storeType;
      return this;
   }


   public IdentityBuilder keyPass(KeyStore.PasswordProtection keyPass)
   {
      this.keyPass = keyPass;
      return this;
   }

   public IdentityBuilder storePass(KeyStore.PasswordProtection storePass)
   {
      this.storePass = storePass;
      return this;
   }



   public IdentityBuilder certificatePath(Path certPath)
      throws NoSuchFileException
   {
      this.certPath = validate(certPath, "CertPath");
      return this;
   }



   public Identity build()
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException,
               UnrecoverableEntryException, InvalidAlgorithmParameterException, CertPathValidatorException
   {
      try {
         if(alias == null) throw new NullPointerException("Identity alias cannot be null");

         KeyStore store = createKeyStoreInstance();
         if(keyStore == null) keyStore = Paths.get(System.getProperty("user.home"), "keystore");
         if(keyStore.equals(NONE)) {
            store.load(() -> storePass);
         } else if(Files.exists(keyStore)) {
            try(InputStream input = Files.newInputStream(keyStore)) {
               store.load(input, (storePass != null) ? storePass.getPassword() : null);
            }
         } else {
            throw new NoSuchFileException("No keystore file could be found");
         }

         KeyStore.PasswordProtection pass = (keyPass != null) ? keyPass : storePass;
         KeyStore.Entry entry = store.getEntry(alias, pass);
         if(!(entry instanceof KeyStore.PrivateKeyEntry)) {
            throw new UnrecoverableKeyException(String.format("Alias %s entry is not a PrivateKey entry", alias));
         }
         KeyStore.PrivateKeyEntry priKeyEntry = (KeyStore.PrivateKeyEntry) entry;

         CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
         CertPath cp = null;
         if(certPath != null) {
            try(InputStream input = Files.newInputStream(certPath)) {
               cp = certificateFactory.generateCertPath(input);
            }
         } else {
            List<Certificate> certificates = Arrays.asList(priKeyEntry.getCertificateChain());
            cp = certificateFactory.generateCertPath(certificates);
         }

         if(cp.getCertificates().isEmpty()) {
            throw new CertificateException("No signing certificate found");
         }


         if(strict) {
            CertPathValidator validator = CertPathValidator.getInstance("PKIX");
            PKIXParameters pkixParameters = new PKIXParameters(createTrustAnchorSet(store, trustStore));
            pkixParameters.setRevocationEnabled(false);
            pkixParameters.setTargetCertConstraints(new CodeSigningCertSelector());
            validator.validate(cp, pkixParameters);
         }

         PrivateKey privateKey = priKeyEntry.getPrivateKey();
         CertPath finalCp = cp;
         return new Identity() {
            @Override
            public String getName()
            {
               return alias;
            }

            @Override
            public PrivateKey getPrivateKey()
            {
               return privateKey;
            }

            @Override
            public Certificate getCertificate()
            {
               return finalCp.getCertificates().get(0);
            }

            @Override
            public CertPath getCertificatePath()
            {
               return finalCp;
            }
         };
      } finally {
         destroy(storePass);
         destroy(keyPass);
      }
   }



   private Path validate(Path path, String ident)
      throws NoSuchFileException
   {
      if(path != null && (!Files.exists(path) || !Files.isReadable(path))) {
         throw new NoSuchFileException(String.format("%s %s not found or is unreadable", ident, path));
      }
      return path;
   }

   private KeyStore createKeyStoreInstance()
      throws KeyStoreException
   {
      String type = (storeType != null) ? storeType : KeyStore.getDefaultType();
      if(provider != null) return KeyStore.getInstance(type, provider);
      return KeyStore.getInstance(type);
   }

   private Set<TrustAnchor> createTrustAnchorSet(KeyStore keystore, Path trustStore)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException
   {
      Set<TrustAnchor> tas = new LinkedHashSet<>();
      try {
         KeyStore caks = getTrustStore(trustStore);
         add(caks, Objects::nonNull, tas);
      } catch (GeneralSecurityException | IOException  e) {
         // Ignore, if cacerts cannot be loaded
         if(trustStore != null) throw e;
      }
      add(keystore, cert -> cert.getSubjectDN().equals(cert.getIssuerDN()), tas);
      return tas ;
   }


   private static void add(KeyStore store, Predicate<X509Certificate> filter, Set<TrustAnchor> tas)
   {
      try {
         Enumeration<String> aliases = store.aliases();
         while(aliases.hasMoreElements()) {
            String a = aliases.nextElement();
            try {
               X509Certificate c = (X509Certificate) store.getCertificate(a);
               if(filter.test(c) || store.isCertificateEntry(a)) {
                  tas.add(new TrustAnchor(c, null));
               }
            } catch(Exception e2) {
               // ignore, when an Entry does not include a cert
            }
         }
      } catch(Exception e) { /* Ignore */ }
   }

   /**
    * Returns the keystore with the configured CA certificates.
    */
   public static KeyStore getTrustStore(Path trustStore)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException
   {
      if(trustStore == null) {
         trustStore = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
      }
      if(!Files.exists(trustStore)) return null;
      try(InputStream in = Files.newInputStream(trustStore)) {
         KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
         store.load(in, null);
         return store;
      }
   }


   private static void destroy(KeyStore.PasswordProtection passwd)
   {
      try {
         passwd.destroy();
      } catch(Exception e) {
         /* Ignore */
      }
   }



   public static class CodeSigningCertSelector implements CertSelector {

      @Override
      public boolean match(Certificate cert)
      {
         if (cert instanceof X509Certificate) {
            X509Certificate xcert = (X509Certificate)cert;
            return isSignatureOrNonRepudiation(xcert)
                     && isAnyOrCodeSigning(xcert);
         }
         return false;
      }

      @Override
      public Object clone()
      {
         try {
            return super.clone();
         } catch(CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
         }
      }

      private boolean isSignatureOrNonRepudiation(X509Certificate xcert)
      {
         boolean[] keyUsage = xcert.getKeyUsage();
         if (keyUsage != null) {
            keyUsage = Arrays.copyOf(keyUsage, 9);
            return keyUsage[0] || keyUsage[1];
         }
         return true;
      }

      private boolean isAnyOrCodeSigning(X509Certificate userCert)
      {
         try {
            List<String> xKeyUsage = userCert.getExtendedKeyUsage();
            if (xKeyUsage != null) {
               if (!xKeyUsage.contains("2.5.29.37.0") // anyExtendedKeyUsage
                  && !xKeyUsage.contains("1.3.6.1.5.5.7.3.3")) {  // codeSigning
                  return false;
               }
            }
         } catch (java.security.cert.CertificateParsingException e) {
            return false;
         }
         return true;
      }

   }
}
