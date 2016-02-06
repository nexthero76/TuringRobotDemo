package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import action.TuringRobot;

public class MainView extends JFrame implements ActionListener, WindowListener, KeyListener{
	/**
	 * @author zifangsky
	 * @blog www.zifangsky.cn
	 * @date 2015-12-21
	 * @version v1.0.0
	 * 
	 * */
	private static final long serialVersionUID = 1L;
	private JPanel mainJPanel,tipJPanel;
	private JScrollPane message_JScrollPane,edit_JScrollPane;
	private JTextArea edit_JTextArea;
	private JButton close,submit;
	private JTextPane messageJTextPane;
	private DefaultStyledDocument doc;
	
	private JMenuBar jMenuBar;
	private JMenu help;
	private JMenuItem author,contact,version,readme;

	private Font contentFont = new Font("宋体", Font.LAYOUT_NO_LIMIT_CONTEXT, 16);  //正文字体
	private Font menuFont = new Font("宋体", Font.LAYOUT_NO_LIMIT_CONTEXT, 14);  //菜单字体
	private Color buttonColor = new Color(85,76,177);  //按钮背景色
	Color inputColor1 = new Color(31,157,255);  //输入相关颜色
	Color inputColor2 = new Color(51,51,51);  //输入相关颜色
	Color outputColor1 = new Color(0,186,4);  //返回相关颜色
	Color outputColor2 = new Color(51,51,51);  //返回相关颜色
	

	private DataOperating dataOperating = null;  //数据操作线程
	private Runnable updateInputInterface,updateResponseInterface;  //更新界面线程
	
	private String inputString = "",responseString = ""; 
	private String key = "";  
	
	public MainView(){
		super("图灵智能聊天");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();  //屏幕大小
		setPreferredSize(new Dimension(350, 600));
		int frameWidth = this.getPreferredSize().width;  //界面宽度
		int frameHeight = this.getPreferredSize().height;  //界面高度
		setSize(frameWidth,frameHeight);
		setLocation((screenSize.width - frameWidth) / 2,(screenSize.height - frameHeight) / 2);
		//初始化
		mainJPanel = new JPanel();
		tipJPanel = new JPanel();
		message_JScrollPane = new JScrollPane();
		edit_JScrollPane = new JScrollPane();
		messageJTextPane = new JTextPane();
		doc = new DefaultStyledDocument();
		edit_JTextArea = new JTextArea(5, 10);
		close = new JButton("关闭");
		submit = new JButton("发送");		
		jMenuBar = new JMenuBar();
		help = new JMenu("帮助");
		author = new JMenuItem("作者");
		contact = new JMenuItem("联系方式");
		version = new JMenuItem("版本");
		readme = new JMenuItem("说明");
		
		//设置字体
		messageJTextPane.setFont(contentFont);
		edit_JTextArea.setFont(contentFont);
		close.setFont(contentFont);
		submit.setFont(contentFont);
		help.setFont(menuFont);
		author.setFont(menuFont);
		contact.setFont(menuFont);
		version.setFont(menuFont);
		readme.setFont(menuFont);
		//布局
		mainJPanel.setLayout(new BorderLayout());
		mainJPanel.add(message_JScrollPane,BorderLayout.NORTH);
		mainJPanel.add(edit_JScrollPane,BorderLayout.CENTER);
		mainJPanel.add(tipJPanel,BorderLayout.SOUTH);
		messageJTextPane.setPreferredSize(new Dimension(350, 350));
		messageJTextPane.setBackground(new Color(204,232,207));
		message_JScrollPane.getViewport().add(messageJTextPane);
		edit_JScrollPane.getViewport().add(edit_JTextArea);
		edit_JTextArea.setBackground(new Color(204,232,207));
		edit_JTextArea.requestFocus();
		tipJPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		tipJPanel.add(close);
		tipJPanel.add(submit);
		close.setBackground(buttonColor);
		close.setForeground(Color.WHITE);
		submit.setBackground(buttonColor);
		submit.setForeground(Color.WHITE);
		
		messageJTextPane.setEditable(false);
		edit_JTextArea.setLineWrap(true);
		edit_JTextArea.setWrapStyleWord(true);
		
		jMenuBar.add(help);
		help.add(author);
		help.add(contact);
		help.add(version);
		help.add(readme);

		add(mainJPanel);
		setJMenuBar(jMenuBar);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);
		//获取配置文件中的配置信息
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("config/config.txt")));
			String line = reader.readLine();  //第一行不要
			Pattern pattern = Pattern.compile("key=(.*)?");
			Matcher matcher;
			while((line = reader.readLine()) != null){
				matcher = pattern.matcher(line);
				if(matcher.find())
					key = matcher.group(1);
				break;	
			}	
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//点击事件
		close.addActionListener(this);
		submit.addActionListener(this);
		author.addActionListener(this);
		contact.addActionListener(this);
		version.addActionListener(this);
		readme.addActionListener(this);
		//键盘时间
		edit_JTextArea.addKeyListener(this);
		//输入对话，提交后触发，更新界面
		updateInputInterface = new Runnable() {
			public void run() {
				messageJTextPane.setEditable(true);
				
				setInputString("我[" + getDateString() + "]：", inputColor1, true, contentFont);
				setInputString(inputString + "\n", inputColor2, false, menuFont);
				messageJTextPane.selectAll();
				messageJTextPane.setCaretPosition(messageJTextPane.getSelectionEnd());
				messageJTextPane.setEditable(false);
			}
		};
		//获取到回答后触发，更新界面
		updateResponseInterface = new Runnable() {
			public void run() {
				messageJTextPane.setEditable(true);

				setResponseString("智子[" + getDateString() + "]：", outputColor1, true, contentFont);
				setResponseString(responseString + "\n", outputColor2, false, menuFont);
				messageJTextPane.selectAll();
				messageJTextPane.setCaretPosition(messageJTextPane.getSelectionEnd());
				messageJTextPane.setEditable(false);
				
				inputString = "";
				responseString = "";
				edit_JTextArea.setText("");
				edit_JTextArea.requestFocus();
				dataOperating = null;
			}
		};
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainView();
			}
		});
	}

	/**
	 * 输入的信息在界面显示出来
	 * */
	private void setInputString(String str,Color color,boolean bold,Font font){
		MutableAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attributeSet, color);  //设置文字颜色
		if(bold)
			StyleConstants.setBold(attributeSet, true);  //设置加粗
		StyleConstants.setFontFamily(attributeSet, "Consolas");  //设置字体
		StyleConstants.setFontSize(attributeSet, font.getSize());  //设置字体大小
		StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_RIGHT);  //左对齐
		insertText(str,attributeSet);
	}
	/**
	 * 返回的信息在界面显示出来
	 * */
	private void setResponseString(String str,Color color,boolean bold,Font font){
		MutableAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attributeSet, color);
		if(bold)
			StyleConstants.setBold(attributeSet, true);
		StyleConstants.setFontFamily(attributeSet, "Consolas");
		StyleConstants.setFontSize(attributeSet, font.getSize());
		StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_LEFT);
		insertText(str,attributeSet);
	}

	/**
	 * 在JTextPane中插入文字
	 * */
	private void insertText(String str, MutableAttributeSet attributeSet) {
		messageJTextPane.setStyledDocument(doc);
		str += "\n";
		doc.setParagraphAttributes(doc.getLength(), str.length(), attributeSet, false);
		try {
			doc.insertString(doc.getLength(), str, attributeSet);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 点击事件
	 * */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == close){
			System.exit(0);
		}
		else if(e.getSource() == submit){
			if(dataOperating == null){
				dataOperating = new DataOperating();
				new Thread(dataOperating).start();
			}
		}
		else if(e.getSource() == author){
			JOptionPane.showMessageDialog(this, "zifangsky","作者：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == contact){
			JOptionPane.showMessageDialog(this, "邮箱：admin@zifangsky.cn\n" +
					"博客：www.zifangsky.cn","联系方式：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == version){
			JOptionPane.showMessageDialog(this, "v1.0.0","版本号：",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getSource() == readme){
			JOptionPane.showMessageDialog(this, "本程序只是简单的智能聊天，没有多余的功能。源码已经在我博客进行开源，\n" +
					"有需求的可以在此基础上进行APP开发，移植到Android平台上去。","说明：",JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	/**
	 * 具体的数据处理内部类
	 * */
	private class DataOperating implements Runnable{
		public void run() {
			//获取输入
			inputString = edit_JTextArea.getText().trim();
			if(inputString == null || "".equals(inputString))
				return;
			SwingUtilities.invokeLater(updateInputInterface);
			//获取回复
			responseString = TuringRobot.getResponse(key, inputString);
			SwingUtilities.invokeLater(updateResponseInterface);
		}
		
	}
	
	/**
	 * 获取当前时间的字符串
	 * @return 当前时间的字符串
	 * */
	private String getDateString(){
		Date date = new Date();
		Format format = new SimpleDateFormat("HH:mm:ss");
		return format.format(date);				
	}

	public void windowOpened(WindowEvent e) {
		
	}

	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

	public void windowClosed(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowActivated(WindowEvent e) {
		
	}

	public void windowDeactivated(WindowEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		
	}
	/**
	 * 键盘事件，键盘按下ENTER键触发
	 * */
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			if(dataOperating == null){
				dataOperating = new DataOperating();
				new Thread(dataOperating).start();
			}
		}
		
	}

	public void keyReleased(KeyEvent e) {
		
	}

}
