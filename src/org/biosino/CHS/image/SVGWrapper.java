package org.biosino.CHS.image;

import java.io.*;
import java.awt.*;

import org.apache.batik.svggen.*;
import org.apache.batik.dom.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;

/**
 * Wrap an image using scalar vector graphics (svg) format.
 */
public class SVGWrapper implements Wrapper {
    /**
     * The <CODE>Rendering</CODE> object to be rendered.
     */
    private Rendering iRendering;
    
    /**
     * The svg document.
     */
    private SVGDocument doc;
    
    /**
     * The <CODE>SVGGraphics2D</CODE> object representing graphical device.
     */
    private SVGGraphics2D g2;
    
    /**
     * Create a new <CODE>SVGWrapper</CODE> object.
     */
    public SVGWrapper () {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        this.doc = (SVGDocument)impl.createDocument(svgNS, "svg", null);
        this.g2 = new SVGGraphics2D(this.doc);
    }
    
    public void Wrap (Rendering iRendering) {
        this.iRendering = iRendering;
        iRendering.render(this.g2);
    }
    
    public Rendering getRendering() {
        return this.iRendering;
    }
    
    /**
     * Get the svg document.
     * @return the svg document
     */
    public SVGDocument getSVGDoc() {
        this.g2.getRoot(this.doc.getDocumentElement());
        return this.doc;
    }
    
    /**
     * Write the svg document to an output file.
     * @param fileName output file name
     * @throws java.io.IOException IOException
     */
    public void saveRendering(String fileName) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(
            new FileWriter(fileName)));
        this.g2.stream(this.doc.getDocumentElement(), out);
    }
}
