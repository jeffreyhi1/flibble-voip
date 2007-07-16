package ui;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class Settings
{
    private String username;
    private String password;
    private String proxy;
    private static Settings instance;
    private String filename = System .getProperty("user.home") + "/.flibblephone" ;
    
    public static synchronized Settings getInstance()
    {
        if (instance == null)
        {
            instance = new Settings();
        }
        return instance;
    }
    
    private Settings()
    {

    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void load()
    {
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream(filename));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        username = props.getProperty("username");
        password = props.getProperty("password");
        proxy = props.getProperty("proxy");
    }
    
    public void save()
    {
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);
        props.setProperty("proxy", proxy);
        File outfile = new File(filename);
        try
        {
            outfile.createNewFile();
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        try
        {
            props.store(new FileOutputStream(filename), null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public boolean existsAndComplete()
    {
        boolean bRet = false;
        File f = new File(filename);
        if (true == f.exists())
        {
            load();
            if (null != username && 
                null != proxy)
            {
                bRet = true;
            }
        }
        return bRet;
    }

    public String getProxy()
    {
        return proxy;
    }

    public void setProxy(String proxy)
    {
        this.proxy = proxy;
    }
    
    
}
