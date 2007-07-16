package ui;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JToggleButton;

import sip.UserAgent;

public class DialPadPanel extends JPanel
{

    private static final long serialVersionUID = 1L;
    private JButton btn2 = null;
    private JButton btn4 = null;
    private JButton btn5 = null;
    private JButton btn6 = null;
    private JButton btn7 = null;
    private JButton btn8 = null;
    private JButton btn9 = null;
    private JButton btnStar = null;
    private JButton btn0 = null;
    private JButton btn1 = null;
    private JButton btn3 = null;
    private JButton btnPound = null;
    /**
     * This is the default constructor
     */
    public DialPadPanel()
    {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setColumns(3);
        gridLayout.setRows(4);
        this.setLayout(gridLayout);
        this.setSize(222, 128);
        this.add(getBtn1(), null);
        this.add(getBtn2(), null);
        this.add(getBtn3(), null);
        this.add(getBtn4(), null);
        this.add(getBtn5(), null);
        this.add(getBtn6(), null);
        this.add(getBtn7(), null);
        this.add(getBtn8(), null);
        this.add(getBtn9(), null);
        this.add(getBtnStar(), null);
        this.add(getBtn0(), null);
        this.add(getBtnPound(), null);
    }

    /**
     * This method initializes btn2	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn2()
    {
        if (btn2 == null)
        {
            btn2 = new JButton();
            btn2.setText("2 abc");
            btn2.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(2);
                }
            });
        }
        return btn2;
    }

    /**
     * This method initializes btn4	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn4()
    {
        if (btn4 == null)
        {
            btn4 = new JButton();
            btn4.setText("4 ghi");
            btn4.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(4);
                }
            });
        }
        return btn4;
    }

    /**
     * This method initializes btn5	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn5()
    {
        if (btn5 == null)
        {
            btn5 = new JButton();
            btn5.setText("5 jkl");
            btn5.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(5);
                }
            });
        }
        return btn5;
    }

    /**
     * This method initializes btn6	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn6()
    {
        if (btn6 == null)
        {
            btn6 = new JButton();
            btn6.setText("6 mno");
            btn6.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(6);
                }
            });
        }
        return btn6;
    }

    /**
     * This method initializes btn7	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn7()
    {
        if (btn7 == null)
        {
            btn7 = new JButton();
            btn7.setText("7 pqrs");
            btn7.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(7);
                }
            });
        }
        return btn7;
    }

    /**
     * This method initializes btn8	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn8()
    {
        if (btn8 == null)
        {
            btn8 = new JButton();
            btn8.setText("8 tuv");
            btn8.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(8);
                }
            });
        }
        return btn8;
    }

    /**
     * This method initializes btn9	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn9()
    {
        if (btn9 == null)
        {
            btn9 = new JButton();
            btn9.setText("9 wxyz");
            btn9.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(9);
                }
            });
        }
        return btn9;
    }

    /**
     * This method initializes btnStar	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtnStar()
    {
        if (btnStar == null)
        {
            btnStar = new JButton();
            btnStar.setText("*");
            btnStar.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(10);
                }
            });
        }
        return btnStar;
    }

    /**
     * This method initializes btn0	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn0()
    {
        if (btn0 == null)
        {
            btn0 = new JButton();
            btn0.setText("0");
            btn0.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(0);
                }
            });
        }
        return btn0;
    }

    /**
     * This method initializes btn1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn1()
    {
        if (btn1 == null)
        {
            btn1 = new JButton();
            btn1.setText("1");
            btn1.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(1);
                }
            });
        }
        return btn1;
    }

    /**
     * This method initializes btn3	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtn3()
    {
        if (btn3 == null)
        {
            btn3 = new JButton();
            btn3.setText("3 def");
            btn3.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(3);
                }
            });
        }
        return btn3;
    }

    /**
     * This method initializes btnPound	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtnPound()
    {
        if (btnPound == null)
        {
            btnPound = new JButton();
            btnPound.setText("#");
            btnPound.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    UserAgent.getInstance().onDialPad(11);
                }
            });
        }
        return btnPound;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
