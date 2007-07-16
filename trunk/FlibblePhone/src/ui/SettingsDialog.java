package ui;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import sip.UserAgent;

import java.awt.Dimension;

public class SettingsDialog extends JDialog
{

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JLabel usernameLabel = null;

    private JTextField usernameCtl = null;

    private JLabel passwordLabel = null;

    private JTextField passwordCtl = null;

    private JLabel proxyLabel = null;

    private JTextField proxyCtl = null;

    private JButton okBtn = null;

    private JLabel filler = null;

    /**
     * @param owner
     */
    public SettingsDialog(Frame owner)
    {
        super(owner);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setSize(458, 141);
        this.setTitle("Flibble Settings");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            filler = new JLabel();
            filler.setText("     (changes require restart)");
            proxyLabel = new JLabel();
            proxyLabel.setText(" SIP Proxy");
            passwordLabel = new JLabel();
            passwordLabel.setText(" Password");
            usernameLabel = new JLabel();
            usernameLabel.setText(" Username");
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(4);
            gridLayout.setHgap(0);
            gridLayout.setVgap(0);
            gridLayout.setColumns(2);
            jContentPane = new JPanel();
            jContentPane.setLayout(gridLayout);
            jContentPane.add(usernameLabel, null);
            jContentPane.add(getUsernameCtl(), null);
            jContentPane.add(passwordLabel, null);
            jContentPane.add(getPasswordCtl(), null);
            jContentPane.add(proxyLabel, null);
            jContentPane.add(getProxyCtl(), null);
            jContentPane.add(filler, null);
            jContentPane.add(getOkBtn(), null);
            populate();
        }
        return jContentPane;
    }

    /**
     * This method initializes usernameCtl	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getUsernameCtl()
    {
        if (usernameCtl == null)
        {
            usernameCtl = new JTextField();
        }
        return usernameCtl;
    }

    /**
     * This method initializes passwordCtl	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getPasswordCtl()
    {
        if (passwordCtl == null)
        {
            passwordCtl = new JTextField();
        }
        return passwordCtl;
    }

    /**
     * This method initializes proxyCtl	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getProxyCtl()
    {
        if (proxyCtl == null)
        {
            proxyCtl = new JTextField();
        }
        return proxyCtl;
    }

    private void populate()
    {
        Settings.getInstance().load();
        if (null != Settings.getInstance().getUsername())
        {
            usernameCtl.setText(Settings.getInstance().getUsername());
        }
        if (null != Settings.getInstance().getPassword())
        {
            passwordCtl.setText(Settings.getInstance().getPassword());
        }
        if (null != Settings.getInstance().getProxy())
        {
            proxyCtl.setText(Settings.getInstance().getProxy());
        }
    }
    
    /**
     * This method initializes okBtn	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOkBtn()
    {
        if (okBtn == null)
        {
            okBtn = new JButton();
            okBtn.setText("OK");
            okBtn.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    Settings.getInstance().setUsername(usernameCtl.getText().trim());
                    Settings.getInstance().setPassword(passwordCtl.getText().trim());
                    Settings.getInstance().setProxy(proxyCtl.getText().trim());
                    Settings.getInstance().save();
                    UserAgent.getInstance().init();            
                    setVisible(false);
                    dispose();
                }
            });
        }
        return okBtn;
    }

}  //  @jve:decl-index=0:visual-constraint="73,4"
