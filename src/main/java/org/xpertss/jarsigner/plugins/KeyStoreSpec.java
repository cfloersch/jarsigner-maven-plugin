package org.xpertss.jarsigner.plugins;

import org.apache.maven.shared.utils.StringUtils;

import java.io.File;

/**
 * POJO containing keystore configuration
 */
public class KeyStoreSpec {

    // NOTE: No point in using @Parameter annotations as they are not used

    private File path;
    private String storepass;
    private String storetype;
    private String provider;


    /**
     * The password used to protect the keystore integrity.
     */
    public String getStorePass()
    {
        return storepass;
    }

    /**
     * The password used to protect the keystore integrity.
     */
    public void setStorePass(String storepass)
    {
        this.storepass = storepass;
    }


    /**
     * The file path to the keystore.
     */
    public File getPath()
    {
        return path;
    }

    /**
     * The file path to the keystore.
     */
    public void setPath(File path)
    {
        this.path = path;
    }


    /**
     * The key store TYPE (JKS, PKCS12, etc). Default is JKS.
     */
    public String getStoreType()
    {
        return storetype;
    }

    /**
     * The key store TYPE (JKS, PKCS12, etc). Default is JKS.
     */
    public void setStoreType(String storetype)
    {
        this.storetype = storetype;
    }


    /**
     * The provider for the keystore (SUN, BC, etc). Default is the
     * default provider for the storetype.
     */
    public String getProvider()
    {
        return provider;
    }

    /**
     * The provider for the keystore (SUN, BC, etc). Default is the
     * default provider for the storetype.
     */
    public void setProvider(String provider)
    {
        this.provider = provider;
    }



    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if(path != null)
            builder.append(String.format("path=%s", path));
        if(StringUtils.isNotEmpty(storetype)) {
            if(builder.length() > 0) builder.append(", ");
            builder.append(String.format("storetype=%s", storetype));
        }
        if(StringUtils.isNotEmpty(storepass)) {
            if(builder.length() > 0) builder.append(", ");
            builder.append(String.format("storepass=%s", storepass));
        }
        if(StringUtils.isNotEmpty(provider)) {
            if(builder.length() > 0) builder.append(", ");
            builder.append(String.format("provider=%s", provider));
        }
        return builder.insert(0, "{").append("}").toString();
    }

}
