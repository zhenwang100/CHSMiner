package org.biosino.CHS.image;

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

/**
 * Wrap an image using post script format (vector map).
 */
public class PsWrapper implements Printable, Wrapper {

    /**
     * The output .ps file.
     */
    private String psFile;
    
    /**
     * The <CODE>Rendering</CODE> object to be rendered.
     */
    private Rendering iRendering;

    /**
     * Create a new <CODE>ImageWrapper</CODE> object.
     * @param psFile the name of output .ps file 
     */
    public PsWrapper(String psFile) {
        this.psFile = psFile;
    }

    public void Wrap(Rendering iRendering) throws Exception {
        this.iRendering = iRendering;

        /* Use the pre-defined flavor for a Printable from an InputStream */
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

        /* Specify the type of the output stream */
        String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();

        /* Locate factory which can export an image stream as Postscript */
        StreamPrintServiceFactory[] factories =
                StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, psMimeType);

        /* Create a file for the exported postscript */
        FileOutputStream fos = new FileOutputStream(this.psFile);

        /* Create a Stream printer for Postscript */
        StreamPrintService sps = factories[0].getPrintService(fos);

        /* Create and call a Print Job */
        DocPrintJob pj = sps.createPrintJob();
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(OrientationRequested.LANDSCAPE);

        Doc doc = new SimpleDoc(this, flavor, null);

        pj.print(doc, aset);
        fos.close();
    }
    
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex == 0) {
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getWidth() / 2, pf.getHeight() / 2); // modify location
            g2.scale(0.5, 0.5); // modeify scale
            g2.translate(-800 / 2, -500 / 2); // modify location again after scale
            this.iRendering.render(g2); //render method is invoked here 
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    public Rendering getRendering() {
        return this.iRendering;
    }
}

