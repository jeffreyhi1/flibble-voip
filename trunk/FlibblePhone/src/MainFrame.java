
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Color;

public class MainFrame
{

    private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

        /* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
         * for the correct SWT library path in order to run with the SWT dlls. 
         * The dlls are located in the SWT plugin jar.  
         * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
         *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
         */
        Display display = Display.getDefault();
        MainFrame thisClass = new MainFrame();
        thisClass.createSShell();
        thisClass.sShell.open();
        while (!thisClass.sShell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    /**
     * This method initializes sShell
     */
    private void createSShell()
    {
        sShell = new Shell(SWT.DIALOG_TRIM);
        sShell.setText("Flibble");
        sShell.setBackground(new Color(Display.getCurrent(), 128, 128, 128));
        sShell.setSize(new Point(166, 309));
        sShell.setLayout(new GridLayout());
        sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter()
        {
            public void shellClosed(org.eclipse.swt.events.ShellEvent e)
            {
                System.exit(0);
            }
        });
        new MainForm(sShell, SWT.NONE);
    }

}
