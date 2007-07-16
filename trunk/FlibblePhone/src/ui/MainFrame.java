package ui;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

public class MainFrame extends JFrame
{

    public static MainFrame thisClass;
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private MainForm mainForm = null;

    /**
     * This method initializes mainForm	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMainForm()
    {
        if (mainForm == null)
        {
            mainForm = new MainForm();
            mainForm.setLayout(null);
        }
        return mainForm;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                thisClass = new MainFrame();
                thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                thisClass.setVisible(true);
            }
        });
    }

    /**
     * This is the default constructor
     */
    public MainFrame()
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
        this.setSize(279, 323);
        this.setContentPane(getJContentPane());
        this.setTitle("Flibble Phone");
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
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            jContentPane = new JPanel();
            jContentPane.setLayout(gridLayout);
            jContentPane.add(getMainForm(), null);
        }
        return jContentPane;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
