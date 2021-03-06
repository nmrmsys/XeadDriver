package xeadDriver;

/*
 * Copyright (c) 2014 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

////////////////////////////////////////////////////////////////
// This is a public class used in Table-Script.               //
// Note that public classes are defined in its own java file. //
////////////////////////////////////////////////////////////////
public class XFInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
    private JPanel jPanelTop = new JPanel();
    private JPanel jPanelCenter = new JPanel();
    private JPanel jPanelBottom = new JPanel();
    private JPanel jPanelButtons = new JPanel();
    private JButton jButtonClose = new JButton();
    private JButton jButtonOK = new JButton();
    private JTextArea jTextArea = new JTextArea();
    private JScrollPane jScrollPane = new JScrollPane();
    private Component parent_;
    private Dimension scrSize, dlgSize;
    private ArrayList<XFInputDialogField> fieldList = new ArrayList<XFInputDialogField>();
    private ArrayList<Integer> areaIndexList = new ArrayList<Integer>();
    private int reply;
	private int nextLocationTopY = 6;
	private int nextLocationCenterY = 6;
	private int messageHeight = 100;
	private int dialogWidth = 230;
	private Session session_;

    public XFInputDialog(Session session) {
		super();
		this.setModal(true);
		this.parent_ = session;
		this.session_ = session;
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		jPanelTop.setLayout(null);
		jPanelTop.setBorder(BorderFactory.createEtchedBorder());
		jPanelCenter.setLayout(null);
		jPanelBottom.setLayout(new BorderLayout());
		jPanelButtons.setLayout(null);
		jTextArea.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextArea.setEditable(false);
		jTextArea.setFocusable(false);
		jTextArea.setLineWrap(true);
		jTextArea.setWrapStyleWord(true);
		jScrollPane.getViewport().add(jTextArea);
		jScrollPane.setFocusable(false);
		jButtonClose.setFont(new java.awt.Font("Dialog", 0, 14));
		jButtonClose.setText(XFUtility.RESOURCE.getString("Close"));
		jButtonClose.addActionListener(new XFInputDialog_jButtonClose_actionAdapter(this));
		jButtonOK.setFont(new java.awt.Font("Dialog", 0, 14));
		jButtonOK.setText("OK");
		jButtonOK.addActionListener(new XFInputDialog_jButtonOK_actionAdapter(this));
		jPanelButtons.add(jButtonClose);
		jPanelButtons.add(jButtonOK);
		jPanelBottom.add(jScrollPane, BorderLayout.CENTER);
		jPanelBottom.add(jPanelButtons,  BorderLayout.SOUTH);
		this.getContentPane().add(jPanelBottom,  BorderLayout.SOUTH);
		this.getRootPane().setDefaultButton(jButtonOK);
		this.setResizable(false);
    }

    public void clear() {
    	jPanelTop.removeAll();
    	jPanelCenter.removeAll();
    	fieldList.clear();
    	areaIndexList.clear();
    	nextLocationTopY = 6;
    	nextLocationCenterY = 6;
    	jTextArea.setText("");
    	messageHeight = 100;
    	jButtonClose.requestFocus();
    }

    public void setMessage(String message) {
    	jTextArea.setText(message);
    }

    public void setMessageHeight(int height) {
    	messageHeight = height;
    }

    public void setWidth(int width) {
    	dialogWidth = width;
    }

    public XFInputDialogField addField(String caption, String inputType) {
    	return addField(caption, inputType, 0, "");
    }

    public XFInputDialogField addField(String caption, String inputType, int areaIndex) {
    	return addField(caption, inputType, areaIndex, "");
    }

    public XFInputDialogField addField(String caption, String inputType, int areaIndex, String parmID) {
    	/////////////////////////////////////////////////////////////////////////////
    	// inputType: ALPHA, KANJI, NUMERIC, DATE, LISTBOX, CHECKBOX, FILE_CHOOSER //
    	// areaIndex: 0=top, 1=center, -1=hidden                                   //
    	/////////////////////////////////////////////////////////////////////////////
    	if (areaIndex != 0 && areaIndex != 1 && areaIndex != -1) {
    		areaIndex = 0;
    	}
		XFInputDialogField field = new XFInputDialogField(caption, inputType, parmID, this);
		if (areaIndex == 0) {
			field.setBounds(new Rectangle(5, nextLocationTopY, field.getBounds().width, field.getBounds().height));
			nextLocationTopY = nextLocationTopY + 28;
			jPanelTop.add(field);
		}
		if (areaIndex == 1) {
			field.setBounds(new Rectangle(5, nextLocationCenterY, field.getBounds().width, field.getBounds().height));
			nextLocationCenterY = nextLocationCenterY + 28;
			jPanelCenter.add(field);
		}
		fieldList.add(field);
		areaIndexList.add(areaIndex);
		return field;
    }

    public int request(String title) {
    	reply = -1;
    	//
    	XFInputDialogField firstEditableFieldOnTopPanel = null;
    	XFInputDialogField firstEditableFieldOnCenterPanel = null;
    	int wrkInt, width = 230;
	    for (int i = 0; i < fieldList.size(); i++) {
	    	if (fieldList.get(i).isEditable()
	    			&& areaIndexList.get(i) == 0
	    			&& firstEditableFieldOnTopPanel == null) {
	    		firstEditableFieldOnTopPanel = fieldList.get(i);
	    	}
	    	if (fieldList.get(i).isEditable()
	    			&& areaIndexList.get(i) == 1
	    			&& firstEditableFieldOnCenterPanel == null) {
	    		firstEditableFieldOnCenterPanel = fieldList.get(i);
	    	}
	    	//wrkInt = fieldList.get(i).getBounds().width + 50;
	    	wrkInt = fieldList.get(i).getBounds().width + 30;
	    	if (wrkInt > width) {
	    		width = wrkInt;
	    	}
	    }
	    if (dialogWidth > width) {
	    	width = dialogWidth;
	    }
	    //
	    if (firstEditableFieldOnTopPanel != null) {
	    	firstEditableFieldOnTopPanel.requestFocus();
	    } else {
		    if (firstEditableFieldOnCenterPanel != null) {
		    	firstEditableFieldOnCenterPanel.requestFocus();
		    } else {
		    	jButtonClose.requestFocus();
		    }
	    }
	    //
	    if (nextLocationTopY > 6 && nextLocationCenterY > 6) {
	    	dlgSize = new Dimension(width, nextLocationTopY + 2 + nextLocationCenterY + 4 + messageHeight + 32);
	    	jPanelTop.setPreferredSize(new Dimension(width, nextLocationTopY + 2));
	    	this.getContentPane().add(jPanelTop,  BorderLayout.NORTH);
	    	this.getContentPane().add(jPanelCenter,  BorderLayout.CENTER);
	    }
	    if (nextLocationTopY == 6 && nextLocationCenterY > 6) {
	    	dlgSize = new Dimension(width, nextLocationCenterY + 2 + messageHeight + 32);
			this.getContentPane().remove(jPanelTop);
	    	this.getContentPane().add(jPanelCenter,  BorderLayout.CENTER);
	    }
	    if (nextLocationTopY > 6 && nextLocationCenterY == 6) {
	    	dlgSize = new Dimension(width, nextLocationTopY + 4 + messageHeight + 32);
	    	this.getContentPane().add(jPanelTop,  BorderLayout.CENTER);
			this.getContentPane().remove(jPanelCenter);
	    }
	    if (nextLocationTopY == 6 && nextLocationCenterY == 6) {
	    	dlgSize = new Dimension(width, messageHeight + 32);
			this.getContentPane().remove(jPanelTop);
			this.getContentPane().remove(jPanelCenter);
	    }
    	jPanelBottom.setPreferredSize(new Dimension(width, messageHeight));
    	jPanelButtons.setPreferredSize(new Dimension(width, 34));
		//
		wrkInt = (width - 220) / 2;
		jButtonClose.setBounds(wrkInt, 2, 80, 30);
		jButtonOK.setBounds(wrkInt + 140, 2, 80, 30);
    	jTextArea.setCaretPosition(0);
    	//
		JLabel jLabel = new JLabel();
		FontMetrics metricsTitle = jLabel.getFontMetrics(this.getFont());
		int titleWidth = metricsTitle.stringWidth(title) + 50;
		if (titleWidth > width) {
			width = titleWidth;
	    	dlgSize = new Dimension(width, dlgSize.height);
		}
    	this.setPreferredSize(dlgSize);
    	this.setTitle(title);
    	if (parent_ != null && parent_.isValid()) {
    		int posY, posX;
    		Rectangle rec = parent_.getBounds();
    		Point point = parent_.getLocationOnScreen();
    		posX = point.x;
    		posY = point.y + rec.height;
    		if (posY + dlgSize.height > scrSize.height) {
    			posY = point.y - dlgSize.height;
    		}
    		this.setLocation(posX, posY);
    	} else {
    		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
    	}
		this.pack();
    	this.setVisible(true);
    	//
    	return reply;
    }

	public Object getValueOfFieldByParmID(String parmID) {
		Object obj = null;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getParmID().equals(parmID)) {
				obj = fieldList.get(i).getValue();
				break;
			}
		}
		return obj;
	}
	
	public ArrayList<XFInputDialogField> getFieldList() {
		return fieldList;
	}
	
	public Session getSession() {
		return session_;
	}

	void jButtonClose_actionPerformed(ActionEvent e) {
		this.setVisible(false);
	}

	void jButtonOK_actionPerformed(ActionEvent e) {
		reply = 0;
		this.setVisible(false);
	}
}

class XFInputDialog_jButtonOK_actionAdapter implements java.awt.event.ActionListener {
	XFInputDialog adaptee;
	XFInputDialog_jButtonOK_actionAdapter(XFInputDialog adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jButtonOK_actionPerformed(e);
	}
}

class XFInputDialog_jButtonClose_actionAdapter implements java.awt.event.ActionListener {
	XFInputDialog adaptee;
	XFInputDialog_jButtonClose_actionAdapter(XFInputDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonClose_actionPerformed(e);
  }
}

