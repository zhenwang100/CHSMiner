package org.biosino.CHS.image;

/**
 * The interface represents graphic devices that can render a
 * <CODE>Rendering</CODE> object.
 */

public interface Wrapper {
    
    /**
     * Wrap a <CODE>Rendering</CODE> object. It always invokes the 
     * <CODE>Render</CODE> method of the <CODE>Rendering</CODE> object.
     * @param iRendering a <CODE>Rendering</CODE> object
     * @throws java.lang.Exception any exception
     */
    public void Wrap(Rendering iRendering) throws Exception;
    
    /**
     * Get the <CODE>Rendering</CODE> object wrapped.
     * @return the <CODE>Rendering</CODE> object
     */
    public Rendering getRendering();
}
