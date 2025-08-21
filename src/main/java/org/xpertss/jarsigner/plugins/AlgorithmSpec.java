package org.xpertss.jarsigner.plugins;

import org.apache.maven.shared.utils.StringUtils;


public class AlgorithmSpec {

    private String algorithm;
    private String provider;

    /**
     * The JCE algorithm name
     */
    public String getAlgorithm()
    {
        return algorithm;
    }

    /**
     * The JCE algorithm name
     */
    public void setAlgorithm(String algorithm)
    {
        this.algorithm = algorithm;
    }

    /**
     * The name of the JCE provider that implements the specified algorithm
     */
    public String getProvider()
    {
        return provider;
    }

    /**
     * The name of the JCE provider that implements the specified algorithm
     */
    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    /**
     * Check if both algorithm and provider are null or empty
     */
    public boolean isNull()
    {
        return StringUtils.isEmpty(algorithm)
                    && StringUtils.isEmpty(provider);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if(StringUtils.isNotEmpty(algorithm))
            builder.append(String.format("algorithm=%s", algorithm));
        if(StringUtils.isNotEmpty(provider)) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(String.format("provider=%s", provider));
        }
        return builder.insert(0,"{").append("}").toString();
    }

}
