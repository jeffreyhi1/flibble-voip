package ui;

import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JTextArea;
import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.Point;
import javax.swing.JTextField;
import javax.swing.JLabel;


import sip.UserAgent;
import ui.SettingsDialog;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;



public class MainForm extends JPanel
{

    private static final long serialVersionUID = 1L;
    private JButton offHookButton = null;
    private JButton onHookButton = null;
    private JTextField dialStringCtl = null;
    private JButton settingsBtn = null;
    private DialPadPanel dialPad = null;
    public static MainForm instance;
    private JTextField textArea = null;
    public static synchronized MainForm getInstance()
    {
        return instance;
    }
    
    /**
     * This is the default constructor
     */
    public MainForm()
    {
        super();
        instance = this;
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setSize(271, 283);
        this.setLayout(null);
        this.add(getOffHookButton(), null);
        this.add(getOnHookButton(), null);
        this.add(getDialStringCtl(), null);
        this.add(getSettingsBtn(), null);
        this.add(getDialPad(), null);
        this.add(getTextArea(), null);
        UserAgent.getInstance().init();
        
    }

    /**
     * This method initializes offHookButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOffHookButton()
    {
        if (offHookButton == null)
        {
            offHookButton = new JButton();
            offHookButton.setText("^");
            offHookButton.setLocation(new Point(21, 45));
            offHookButton.setSize(new Dimension(49, 24));
            offHookButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onCallButtonPressed(dialStringCtl.getText());
                }
            });
        }
        return offHookButton;
    }

    /**
     * This method initializes onHookButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOnHookButton()
    {
        if (onHookButton == null)
        {
            onHookButton = new JButton();
            onHookButton.setLocation(new Point(200, 44));
            onHookButton.setText("x");
            onHookButton.setSize(new Dimension(49, 24));
            onHookButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().bye();
                }
            });
        }
        return onHookButton;
    }

    /**
     * This method initializes dialStringCtl	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getDialStringCtl()
    {
        if (dialStringCtl == null)
        {
            dialStringCtl = new JTextField();
            dialStringCtl.setBounds(new Rectangle(20, 78, 230, 20));
        }
        return dialStringCtl;
    }

    /**
     * This method initializes settingsBtn	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getSettingsBtn()
    {
        if (settingsBtn == null)
        {
            settingsBtn = new JButton();
            settingsBtn.setBounds(new Rectangle(152, 251, 97, 18));
            settingsBtn.setText("Settings...");
            settingsBtn.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    new SettingsDialog(MainFrame.thisClass).show();
                }
            });
        }
        return settingsBtn;
    }

    public void beep()
    {
        new Thread(
                new Runnable()
                {
                    public void run()
                    {
                      try
                      {
                          Toolkit.getDefaultToolkit().beep(); 
                          Thread.sleep(500);
                          Toolkit.getDefaultToolkit().beep();     
                          Thread.sleep(500);
                          Toolkit.getDefaultToolkit().beep();     
                          Thread.sleep(500);
                          Toolkit.getDefaultToolkit().beep();     
                          Thread.sleep(100);
                      }
                      catch (Exception e)
                      {
                          
                      }
                    }
                }).start();
    }
    
    public void addToDialString(String s)
    {
        this.dialStringCtl.setText(dialStringCtl.getText()+s);
    }
    
    public void setIncomingCallerId(final String callerId)
    {
        new Thread(
                new Runnable()
                {
                    public void run()
                    {
                          textArea.setText(callerId);
                    }
                }).start();        
    }
    
    /**
     * This method initializes dialPad	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getDialPad()
    {
        if (dialPad == null)
        {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(4);
            gridLayout.setColumns(3);
            dialPad = new DialPadPanel();
            dialPad.setLayout(gridLayout);
            dialPad.setBounds(new Rectangle(22, 110, 227, 126));
        }
        return dialPad;
    }

    /**
     * This method initializes textArea	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTextArea()
    {
        if (textArea == null)
        {
            textArea = new JTextField();
            textArea.setBounds(new Rectangle(21, 12, 229, 20));
            textArea.setEditable(false);
            textArea.setBackground(Color.white);
            textArea.setForeground(new Color(51, 51, 217));
            textArea.setText("Authenticating...");
            textArea.setHorizontalAlignment(JTextField.CENTER);
        }
        return textArea;
    }

}  //  @jve:decl-index=0:visual-constraint="-1,-5"
