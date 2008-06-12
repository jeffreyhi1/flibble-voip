package com.sipresponse.flibblecallmgr.internal.util;

public class StringUtil
{
    public static String reverse(String source)
    {
        int i, len = source.length();
        StringBuffer dest = new StringBuffer(len);

        for (i = (len - 1); i >= 0; i--)
            dest.append(source.charAt(i));
        return dest.toString();
    }

    public static boolean hasAlpha(String s)
    {
        boolean ret = false;
        for (int i = 0; i < s.length(); i++)
        {
            if (Character.isLetter(s.charAt(i)))
            {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public static boolean hasDigits(String s)
    {
        boolean ret = false;
        for (int i = 0; i < s.length(); i++)
        {
            if (Character.isDigit(s.charAt(i)))
            {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public static String stripAllSymbols(String s, boolean allowAsterisk)
    {
        String temp = new String();
        s = s.replaceAll("-", "~");
        s = s.replaceAll("\\(", "~");
        s = s.replaceAll("\\)", "~");
        s = s.replaceAll("\\.", "~");
        s = s.replaceAll("\"", "~");
        s = s.replaceAll("\\*", "~");
        s = s.replaceAll(",", "~");
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) != '~')
            {
                temp += s.charAt(i);
            }
        }
        return temp;
    }

    public static String stripAllSpaces(String s)
    {
        String temp = new String();
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) != ' ')
            {
                temp += s.charAt(i);
            }
        }
        return temp;
    }

    public static String stripAllButNumbers(String s, boolean allowAsterisk)
    {
        String temp = new String();
        for (int i = 0; i < s.length(); i++)
        {
            if (Character.isDigit(s.charAt(i))
                    || (allowAsterisk && s.charAt(i) == '*'))
            {
                temp += s.charAt(i);
            }
        }
        return temp;
    }

    public static void main(String[] args)
    {
        String test1 = "\"1-(781), 729.1709\"";
        System.out.println(test1);
        test1 = StringUtil.stripAllSymbols(test1, false);
        System.out.println(test1);
    }

}
