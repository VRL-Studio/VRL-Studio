/* 
 * HelpPDFViewer.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009–2012 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2007–2012 by Michael Hoffer
 * 
 * This file is part of VRL-Studio.
 *
 * VRL-Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL-Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL-Studio includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL-Studio. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas:
 * "based on VRL source code". In this case the VRL canvas icon must be removed.
 * 
 * Second, keep the links to "About VRL-Studio" and "About VRL". The
 * copyright notice must remain.
 *
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 */

package eu.mihosoft.vrlstudio.main;

import eu.mihosoft.vrl.visual.VGraphicsUtil;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.net.URL;
import javax.swing.*;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PdfPageData;

public class HelpPDFViewer extends JFrame {

    private String viewerTitle = "VRL-Studio Help";
    /**
     * the actual JPanel/decoder object
     */
    private PdfDecoder pdfDecoder;
    /**
     * name of current PDF file
     */
    private String currentFile = null;
    /**
     * current page number (first page is 1)
     */
    private int currentPage = 1;
    private final JLabel pageCounter1 = new JLabel("Page ");
    private JTextField pageCounter2 = new JTextField(4);//000 used to set prefered size
    private JLabel pageCounter3 = new JLabel("of");//000 used to set prefered size

    /**
     * construct a pdf viewer, passing in the full file name
     */
    public HelpPDFViewer(String title, String name, final boolean scaleToWidth) {

        this.viewerTitle = title;

        setTitle(title);

        pdfDecoder = new PdfDecoder(true);

        //ensure non-embedded font map to sensible replacements
        FontMappings.setFontReplacements();

        currentFile = name;//store file name for use in page changer

        try {
            //this opens the PDF and reads its internal details
            pdfDecoder.openPdfFile(currentFile);

            //these 2 lines opens page 1 at 100% scaling
            pdfDecoder.decodePage(currentPage);
            pdfDecoder.setPageParameters(1, 1); //values scaling (1=100%). page number
        } catch (Exception e) {
            e.printStackTrace();
        }

        //setup our GUI display
        initializeViewer(scaleToWidth);

        //set page number display
        pageCounter2.setText(String.valueOf(currentPage));
        pageCounter3.setText("of " + pdfDecoder.getPageCount());

        PdfPageData pageData = pdfDecoder.getPdfPageData();

        int width = pageData.getCropBoxWidth(1);
        int height = pageData.getCropBoxHeight(1) + 45; // topbar offset

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        width = Math.min(width, screen.width/2);
        height = Math.min(height, screen.height/2);

        pack();
        
        setSize(width, height);
       

        getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener() {

            @Override
            public void ancestorMoved(HierarchyEvent e) {
                //
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {

                PdfPageData pageData = pdfDecoder.getPdfPageData();

                float widthScale = pdfDecoder.getVisibleRect().width / (float) pageData.getCropBoxWidth(currentPage);
                float heightScale = pdfDecoder.getVisibleRect().height / (float) pageData.getCropBoxHeight(currentPage);

                float scale = Math.min(widthScale, heightScale);


                if (scaleToWidth) {
                    scale = widthScale;
                }


                pdfDecoder.setPageParameters(scale, currentPage);
            }
        });

    }

    /**
     * opens a chooser and allows user to select a pdf file and opens it
     */
    private void showFile(String file) {


        currentFile = file;

        currentPage = 1;
        try {
            //close the current pdf before opening another
            pdfDecoder.closePdfFile();

//          this opens the PDF and reads its internal details
            pdfDecoder.openPdfFile(currentFile);

            //check for password encription and acertain
            if (!checkEncryption()) {
                //if file content is not accessable make user select a different file
                System.err.println("ERROR: file " + file + "cannot be opened!");
            }

//          these 2 lines opens page 1 at 100% scaling
            pdfDecoder.decodePage(currentPage);
            pdfDecoder.setPageParameters(1, 1); //values scaling (1=100%). page number
            pdfDecoder.invalidate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //set page number display
        pageCounter2.setText(String.valueOf(currentPage));
        pageCounter3.setText("of " + pdfDecoder.getPageCount());

        setTitle(viewerTitle + " - " + currentFile);

        repaint();

    }

    /**
     * check if encryption present and acertain password, return true if content
     * accessable
     */
    private boolean checkEncryption() {

//    check if file is encrypted
        if (pdfDecoder.isEncrypted()) {

            //if file has a null password it will have been decoded and isFileViewable will return true
            while (!pdfDecoder.isFileViewable()) {

                /**
                 * popup window if password needed
                 */
                String password = JOptionPane.showInputDialog(this, "Please enter password");

                /**
                 * try and reopen with new password
                 */
                if (password != null) {
                    try {
                        pdfDecoder.setEncryptionPassword(password);
                    } catch (PdfException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    //pdfDecoder.verifyAccess();

                }
            }
            return true;
        }
        //if not encrypted return true
        return true;
    }

    /**
     * setup the viewer and its components
     */
    private void initializeViewer(boolean scaleToWidth) {

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout());

//        JButton open = initOpenBut();//setup open button
        Component[] itemsToAdd = initChangerPanel();//setup page display and changer

        JPanel topBar = new JPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//    topBar.add(open);
//    topBar.add(pageChanger);
        for (Component anItemsToAdd : itemsToAdd) {
            topBar.add(anItemsToAdd);
        }

        cPane.add(topBar, BorderLayout.NORTH);

        JScrollPane display = initPDFDisplay();//setup scrollpane with pdf display inside
        cPane.add(display, BorderLayout.CENTER);
        display.getVerticalScrollBar().setUnitIncrement(25);
        display.getHorizontalScrollBar().setUnitIncrement(25);

        if (scaleToWidth) {
            display.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }

        setLocationRelativeTo(null);//centre on screen

        setVisible(true);
    }

    /**
     * returns the scrollpane with pdfDecoder set as the viewport
     */
    private JScrollPane initPDFDisplay() {

        JScrollPane currentScroll = new JScrollPane();
        currentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        currentScroll.setViewportView(pdfDecoder);

        return currentScroll;
    }

    /**
     * setup the page display and changer panel and return it
     */
    private Component[] initChangerPanel() {

        Component[] list = new Component[11];

        /**
         * back to page 1
         */
        JButton start = new JButton();
        start.setBorderPainted(false);
        URL startImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/start.gif");
        start.setIcon(new ImageIcon(startImage));
        start.setToolTipText("Rewind to page 1");
//    currentBar1.add(start);
        list[0] = start;
        start.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage != 1) {
                    currentPage = 1;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("back to page 1");
                        e1.printStackTrace();
                    }

                    //set page number display
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });

        /**
         * back 10 icon
         */
        JButton fback = new JButton();
        fback.setBorderPainted(false);
        URL fbackImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/fback.gif");
        fback.setIcon(new ImageIcon(fbackImage));
        fback.setToolTipText("Rewind 10 pages");
//    currentBar1.add(fback);
        list[1] = fback;
        fback.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage > 10) {
                    currentPage -= 10;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("back 10 pages");
                        e1.printStackTrace();
                    }

//            set page number display
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });

        /**
         * back icon
         */
        JButton back = new JButton();
        back.setBorderPainted(false);
        URL backImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/back.gif");
        back.setIcon(new ImageIcon(backImage));
        back.setToolTipText("Rewind one page");
//    currentBar1.add(back);
        list[2] = back;
        back.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage > 1) {
                    currentPage -= 1;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("back 1 page");
                        e1.printStackTrace();
                    }

//          set page number display
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });

        pageCounter2.setEditable(true);
        pageCounter2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent a) {

                String value = pageCounter2.getText().trim();
                int newPage;

                //allow for bum values
                try {
                    newPage = Integer.parseInt(value);

                    if ((newPage > pdfDecoder.getPageCount()) | (newPage < 1)) {
                        return;
                    }

                    currentPage = newPage;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e) {
                        System.err.println("page number entered");
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, '>' + value + "< is Not a valid Value.\nPlease enter a number between 1 and " + pdfDecoder.getPageCount());
                }

            }
        });


        list[3] = pageCounter1;
        list[4] = new JPanel();
        list[5] = pageCounter2;
        list[6] = new JPanel();
        list[7] = pageCounter3;

        /**
         * forward icon
         */
        JButton forward = new JButton();
        forward.setBorderPainted(false);
        URL fowardImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/forward.gif");
        forward.setIcon(new ImageIcon(fowardImage));
        forward.setToolTipText("forward 1 page");
        list[8] = forward;
        forward.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage < pdfDecoder.getPageCount()) {
                    currentPage += 1;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("forward 1 page");
                        e1.printStackTrace();
                    }

//        set page number display
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });

        /**
         * fast forward icon
         */
        JButton fforward = new JButton();
        fforward.setBorderPainted(false);
        URL ffowardImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/fforward.gif");
        fforward.setIcon(new ImageIcon(ffowardImage));
        fforward.setToolTipText("Fast forward 10 pages");
//    currentBar1.add(fforward);
        list[9] = fforward;
        fforward.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage < pdfDecoder.getPageCount() - 9) {
                    currentPage += 10;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("forward 10 pages");
                        e1.printStackTrace();
                    }

//        set page number display
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });

        /**
         * goto last page
         */
        JButton end = new JButton();
        end.setBorderPainted(false);
        URL endImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/end.gif");
        end.setIcon(new ImageIcon(endImage));
        end.setToolTipText("Fast forward to last page");
//    currentBar1.add(end);
        list[10] = end;
        end.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage < pdfDecoder.getPageCount()) {
                    currentPage = pdfDecoder.getPageCount();
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("forward to last page");
                        e1.printStackTrace();
                    }

//        set page number display
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });

        return list;
    }

    /**
     * create a standalone program. User may pass in name of file as option
     */
    public static void newView(String title, String file, boolean scaleToFitWidth) {
        /**
         * Run the software
         */
        new HelpPDFViewer(title, file, scaleToFitWidth);

    }
//    
//    public void snapScalingToDefaults(float newScaling) {
//        newScaling = pdfDecoder.getDPIFactory().adjustScaling(newScaling /100);
//
//        float width,height;
//
////        if(isSingle){
//            width = scrollPane.getViewport().getWidth();
//            height = scrollPane.getViewport().getHeight();
////        }
////        else{
////            width=desktopPane.getWidth();
////            height=desktopPane.getHeight();
////        }
//
//        PdfPageData pageData = decode_pdf.getPdfPageData();
//        int cw,ch,raw_rotation=0;
//
//        if (decode_pdf.getDisplayView()==Display.FACING)
//            raw_rotation=pageData.getRotation(commonValues.getCurrentPage());
//
//        boolean isRotated = (rotation+raw_rotation)%180==90;
//
//        PageOffsets offsets = (PageOffsets)decode_pdf.getExternalHandler(Options.CurrentOffset);
//        switch(decode_pdf.getDisplayView()) {
//            case Display.CONTINUOUS_FACING:
//                if (isRotated) {
//                    cw = offsets.getMaxH()*2;
//                    ch = offsets.getMaxW();
//                }else{
//                    cw = offsets.getMaxW()*2;
//                    ch = offsets.getMaxH();
//                }
//                break;
//            case Display.CONTINUOUS:
//                if (isRotated) {
//                    cw = offsets.getMaxH();
//                    ch = offsets.getMaxW();
//                }else{
//                    cw = offsets.getMaxW();
//                    ch = offsets.getMaxH();
//                }
//                break;
//            case Display.FACING:
//                int leftPage;
//                if (currentCommands.getPages().getSeparateCover()) {
//                    leftPage = (commonValues.getCurrentPage()/2)*2;
//                    if (commonValues.getPageCount() == 2)
//                        leftPage = 1;
//                } else {
//                    leftPage = commonValues.getCurrentPage();
//                    if ((leftPage & 1)==0)
//                        leftPage--;
//                }
//
//                if (isRotated) {
//                    cw = pageData.getCropBoxHeight(leftPage);
//
//                    //if first or last page double the width, otherwise add other page width
//                    if (leftPage+1 > commonValues.getPageCount() || leftPage == 1)
//                        cw = cw * 2;
//                    else
//                        cw += pageData.getCropBoxHeight(leftPage+1);
//
//                    ch = pageData.getCropBoxWidth(leftPage);
//                    if (leftPage+1 <= commonValues.getPageCount() && ch < pageData.getCropBoxWidth(leftPage+1))
//                        ch = pageData.getCropBoxWidth(leftPage+1);
//                }else{
//                    cw = pageData.getCropBoxWidth(leftPage);
//
//                    //if first or last page double the width, otherwise add other page width
//                    if (leftPage+1 > commonValues.getPageCount())
//                        cw = cw * 2;
//                    else
//                        cw += pageData.getCropBoxWidth(leftPage+1);
//
//                    ch = pageData.getCropBoxHeight(leftPage);
//                    if (leftPage+1 <= commonValues.getPageCount() && ch < pageData.getCropBoxHeight(leftPage+1))
//                        ch = pageData.getCropBoxHeight(leftPage+1);
//                }
//                break;
//            default:
//                if (isRotated) {
//                    cw = pageData.getCropBoxHeight(commonValues.getCurrentPage());
//                    ch = pageData.getCropBoxWidth(commonValues.getCurrentPage());
//                }else{
//                    cw = pageData.getCropBoxWidth(commonValues.getCurrentPage());
//                    ch = pageData.getCropBoxHeight(commonValues.getCurrentPage());
//                }
//        }
//
//        //Add space at the bottom for pageFlow
//        if (decode_pdf.getDisplayView()==Display.PAGEFLOW)
//            ch = PageOffsets.getPageFlowExtraHeight(ch);
//
//        if(isSingle){
//            if(displayPane!=null)
//                width = width-displayPane.getDividerSize();
//        }
//
//        float x_factor,y_factor,window_factor;
//        x_factor = width / cw;
//        y_factor = height / ch;
//
//        if(x_factor<y_factor) {
//            window_factor = x_factor;
//            x_factor = -1;
//        } else {
//            window_factor = y_factor;
//            y_factor = -1;
//        }
//
//        if (getSelectedComboIndex(Commands.SCALING)!=0 &&
//                ((newScaling < window_factor * 1.1 && newScaling > window_factor *0.91) ||
//                ((window_factor > scaling && window_factor < newScaling) || (window_factor < scaling && window_factor > newScaling)))) {
//            setSelectedComboIndex(Commands.SCALING, 0);
//            scaling = window_factor;
//        }
//
//        else if (y_factor!=-1 &&
//                getSelectedComboIndex(Commands.SCALING)!=1 &&
//                ((newScaling < y_factor * 1.1 && newScaling > y_factor * 0.91) ||
//                ((y_factor > scaling && y_factor < newScaling) || (y_factor < scaling && y_factor > newScaling)))) {
//            setSelectedComboIndex(Commands.SCALING, 1);
//            scaling = y_factor;
//        }
//
//        else if (x_factor!=-1 &&
//                getSelectedComboIndex(Commands.SCALING)!=2 &&
//                ((newScaling < x_factor * 1.1 && newScaling > x_factor * 0.91) ||
//                ((x_factor > scaling && x_factor < newScaling) || (x_factor < scaling && x_factor > newScaling)))) {
//            setSelectedComboIndex(Commands.SCALING, 2);
//            scaling = x_factor;
//        }
//
//        else {
//            setSelectedComboItem(Commands.SCALING, String.valueOf((int)decode_pdf.getDPIFactory().removeScaling(newScaling *100)));
//            scaling = newScaling;
//        }
//    }
//    /**
//     * create a standalone program. User may pass in name of file as option
//     */
//    public static void main(String[] args) {
//
//
//        /**
//         * Run the software
//         */
//        if (args.length > 0) {
//            new JPanelDemo(args[0]);
//        } else {
//            new JPanelDemo();
//        }
//    }
}
