package com.example.mycloudalbum;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Encoder
{
    public static String encode(String pwd)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(pwd.getBytes("UTF-8"));
            for (int i = 0; i < bytes.length; i++)
            {
                String s = Integer.toHexString(0xff & bytes[i]);

                if (s.length() == 1)
                {
                    sb.append("0" + s);
                }
                else
                {
                    sb.append(s);
                }
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
