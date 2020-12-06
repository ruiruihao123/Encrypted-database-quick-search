package com.ssdb.demo;

//
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.performance.test.PreProcessTest;
import com.performance.test.SQLGenerator;
import com.ssdb.deparser.InsertDeparserOpt;

public class XSDB_GUI2 extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException {
		new ClientFrame2();
	}
}

class ClientFrame2 extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;
	private JPanel headPanel;
	private JPanel footPanel;
	private static JTextPane showArea;
	private ScrollPane scrollPane;
	private ScrollPane scrollPane1;
	private JTextPane sqlArea;
	private JLabel portLbl;
	private JLabel ipLbl;
	private JLabel dbNameLbl;
	private JTextField userName;
	private JPasswordField pwd;
	private JTextField dbNameTxt;
	private JButton submitBtn;
	private JButton bindBtn;
	private JButton checkBtn;
	private JButton loginBtn;
	private JButton clearBtn;
	private StringBuilder stringBuilder=null;
	private JButton batchInsertBtn;

	static String database = null;
	static String url = null;
	static String name = null;
	static String password = null;

	static {
		try {
			// 1. 创建一个属性配置对象
			Properties properties = new Properties();
			InputStream is = new FileInputStream("data.properties");
			properties.load(is);
			database = properties.getProperty("database");
			name = properties.getProperty("user");
			password = properties.getProperty("password");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ClientFrame2() throws IOException {
		init(); // 初始化GUI界面
	}
	
	
	private void init() throws IOException {
		stringBuilder = new StringBuilder();
		this.setTitle("密文数据库速查系统");
		ImageIcon icon = new ImageIcon("2.png");
		this.setIconImage(icon.getImage());
		mainPanel = new JPanel(); // 显示聊天消息的文本域
		showArea = new JTextPane();
		showArea.setEditable(false);
		showArea.setBackground(Color.white);
		showArea.setForeground(Color.BLUE);
		showArea.setText("执行结果显示区：");
		showArea.setFont(new Font("仿宋",Font.PLAIN, 20));
		clearBtn = new JButton("清除");
		scrollPane = new ScrollPane();
		scrollPane.setSize(580, 350);
		scrollPane.add(showArea);
		mainPanel.add(scrollPane);
		mainPanel.add(clearBtn);
		getContentPane().add(mainPanel, BorderLayout.SOUTH);

		headPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		portLbl = new JLabel("用户名");
		userName = new JTextField(7);
		userName.setText("root");
		ipLbl = new JLabel("密码");
		pwd = new JPasswordField(10);
		pwd.setText("xing");
		dbNameLbl = new JLabel("数据库名");
		dbNameTxt = new JTextField(15);
		dbNameTxt.setText("college");
		bindBtn = new JButton("绑定");
		loginBtn = new JButton("登录");
		
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stringBuilder = new StringBuilder();
				showArea.setForeground(Color.BLUE);
				showArea.setText("");
			}
		});
		bindBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tmp = dbNameTxt.getText();
				if (tmp != null && tmp.length() > 0) {
					int id = javax.swing.JOptionPane.showConfirmDialog(null, "确定绑定此数据库吗？", "提示框",
							javax.swing.JOptionPane.OK_OPTION);
					if (id == 0) {
						String dbName = dbNameTxt.getText();
						if (dbName.equals(database)) {
							showArea.setText("数据库绑定成功，现在可以输入sql语句执行！");
							bindBtn.setEnabled(false);
							submitBtn.setEnabled(true);
							checkBtn.setEnabled(true);
							batchInsertBtn.setEnabled(true);
						} else {
							showArea.setText("数据库绑定失败，请重新输入数据库名字！");
						}

					}
				}
			}
		});
		headPanel.add(portLbl);
		headPanel.add(userName);
		headPanel.add(ipLbl);
		headPanel.add(pwd);
		headPanel.add(loginBtn);
		headPanel.add(dbNameLbl);
		headPanel.add(dbNameTxt);
		headPanel.add(bindBtn);
		loginBtn.setEnabled(true);
		bindBtn.setEnabled(false);

		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user_Name = userName.getText();
				String getPwd = String.valueOf(pwd.getPassword());

				if (user_Name.equals(name) && getPwd.equals(password)) {
					loginBtn.setEnabled(false);
					bindBtn.setEnabled(true);
					showArea.setText("登录成功！");
					
				} else {
					showArea.setText("登录失败，用户名或密码输入错误！");
				}
			}
		});
		getContentPane().add(headPanel, BorderLayout.NORTH);

		footPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // 显示要发送的消息输入域
		scrollPane1 = new ScrollPane();
		scrollPane1.setSize(500,80);
		sqlArea = new JTextPane();
		sqlArea.setBackground(Color.white);
		sqlArea.setForeground(Color.DARK_GRAY);
		sqlArea.setFont(new Font("console",Font.PLAIN, 18));
		sqlArea.setText("select id,name from student where id < 50;");
		scrollPane1.add(sqlArea);
		JPanel footPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footPanel2.setPreferredSize(new Dimension(150,75));
		checkBtn = new JButton("检测");
		submitBtn = new JButton("执行sql");
		batchInsertBtn  = new JButton("批量插入测试");
		footPanel2.add(submitBtn);
		footPanel2.add(checkBtn);
		footPanel2.add(batchInsertBtn);
		
		footPanel.add(scrollPane1);
		footPanel.add(footPanel2);
		submitBtn.setEnabled(false);
		checkBtn.setEnabled(false);
		batchInsertBtn.setEnabled(false);
		getContentPane().add(footPanel, BorderLayout.CENTER);
		
		this.setSize(680, 570);
		setLocation(300, 100);
		setVisible(true);
		checkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreProcessTest preTest = new PreProcessTest();
	 			if (stringBuilder!= null && showArea != null) {
					stringBuilder.append("当前默认sens是0.01，请注意数据精度,超出精度值会出现保序操作不准确\n");
					showArea.setForeground(Color.BLUE);
					showArea.setText(stringBuilder.toString());
				}else{
					System.out.println("检测按钮方法发生异常，程序终止");
					System.exit(1);  //异常退出
				}
	 			
	 			preTest.testSSDB(showArea,stringBuilder);
				
			}
		});
		
		
		submitBtn.addActionListener(new ActionListener() { // 执行
			public void actionPerformed(ActionEvent e) {
				String sql = sqlArea.getText().trim();
				if (sql == null || "".equals(sql)) {
					showArea.setText("您输入的sql语句为空！");
				} else {
					showArea.setForeground(Color.RED);
					ClientDemo.RecvToParseSql(sql, showArea, stringBuilder);    //交给ClientDemo执行判断解析
//					sqlArea.setText("");
				}
			}
		});
		batchInsertBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SQLGenerator sqlGenerator = new SQLGenerator();
				String sqlString = sqlGenerator.sqlInsertToStudent(2000);
				if (sqlString == null || "".equals(sqlString)) {
					showArea.setText("您输入的sql语句为空！");
				} else {
					showArea.setForeground(Color.BLUE);
					showArea.setText("正在插入5000条测试sql语句...");
					InsertDeparserOpt insertDep = new InsertDeparserOpt();
					insertDep.insertToStudent(5000, showArea, stringBuilder);
				}				
			}
		});
		
		
		
		addWindowListener(new WindowAdapter() { // 退出客户端
			public void windowClosing(WindowEvent e) {			
				System.exit(0);
			}
		});
		
	}
	
	public static JTextPane getShowArea(){
		return showArea;
	}
	

}