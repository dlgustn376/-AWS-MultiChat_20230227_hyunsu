package views;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;

import dto.request.RequestDto;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ClientApplication extends JFrame {
	
	private Gson gson;
	private Socket socket;
	
	private JPanel mainPanel;
	private CardLayout maincard;
	
	private JTextField usernameField;
	
	private JTextField sendMessageField;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientApplication frame = new ClientApplication();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientApplication() {
		
		
		/*==================<< Init >>==================*/
		gson = new Gson();
		try {
			socket = new Socket("127.0.0.1", 9090);
			ClientRecive clientRecive = new ClientRecive(socket);
			clientRecive.start();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (ConnectException e1) {
			JOptionPane.showMessageDialog(this, "서버에 접속할 수 없습니다.", "접속오류", JOptionPane.ERROR_MESSAGE);
			System.exit(0); // 접속 오류가 실패 했을때, 접속을 안되게 하는 명령
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		/*==================<< Frame set >>==================*/
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 150, 480, 800);
		
		/*==================<< Panels >>==================*/
		
		mainPanel = new JPanel();
		JPanel loginPanel = new JPanel();
		JPanel roomListPanel = new JPanel();
		JPanel roomPanel = new JPanel();

		/*==================<< Layout >>==================*/
		
		maincard = new CardLayout();
		
		mainPanel.setLayout(maincard);
		loginPanel.setLayout(null);
		roomListPanel.setLayout(null);
		roomPanel.setLayout(null);

		/*==================<< Panel set >>==================*/

		setContentPane(mainPanel);
		mainPanel.add(loginPanel, "loginPanel");
		mainPanel.add(roomListPanel, "roomListPanel");
		mainPanel.add(roomPanel, "roomPanel");

		/*==================<< Login Panel >>==================*/
		
		JButton enterButton = new JButton("접속하기");
		
		usernameField = new JTextField();
		usernameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					
					enterButton.doClick();
//				동일한 동작은 하나만 쓰고, 이벤트를 발생시킴.
//				좀 더 간결해짐.
				}
			}
		});
		
		usernameField.setFont(new Font("굴림", Font.BOLD, 18));
		usernameField.setBounds(38, 475, 390, 54);
		loginPanel.add(usernameField);
		usernameField.setColumns(10);
		
		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 클라이언트가 socket 객체를 가져야 함.
				RequestDto<String> usernameCheckReqDto = 
						new RequestDto<String>("usernameCheck", usernameField.getText());
				sendRquest(usernameCheckReqDto);
			}
		});
		enterButton.setBounds(48, 539, 371, 54);
		loginPanel.add(enterButton);
		
		/*==================<< RoomList Panel >>==================*/
		
		JScrollPane roomListScroll = new JScrollPane();
		roomListScroll.setBounds(98, 0, 356, 751);
		roomListPanel.add(roomListScroll);
		
		JList roomList = new JList();
		roomListScroll.setViewportView(roomList);
		
		JButton createButton = new JButton("방 생성");
		createButton.setBounds(12, 10, 74, 78);
		roomListPanel.add(createButton);
		
		/*==================<< Room Panel >>==================*/
		
		JScrollPane joinUserListScroll = new JScrollPane();
		joinUserListScroll.setBounds(0, 0, 348, 103);
		roomPanel.add(joinUserListScroll);
		
		JList joinUserList = new JList();
		joinUserListScroll.setViewportView(joinUserList);
		
		JButton roomExitButton = new JButton("나가기");
		roomExitButton.setBounds(348, 0, 106, 103);
		roomPanel.add(roomExitButton);
		
		JScrollPane chattingContentsScroll = new JScrollPane();
		chattingContentsScroll.setBounds(0, 103, 454, 584);
		roomPanel.add(chattingContentsScroll);
		
		JTextArea chattingContent = new JTextArea();
		chattingContentsScroll.setViewportView(chattingContent);
		
		sendMessageField = new JTextField();
		sendMessageField.setBounds(0, 689, 377, 62);
		roomPanel.add(sendMessageField);
		sendMessageField.setColumns(10);
		
		JButton sendButton = new JButton("전송");
		sendButton.setBounds(378, 689, 76, 62);
		roomPanel.add(sendButton);
		
	}
	
	private void sendRquest(RequestDto<?> requestDto) {
		String reqJson = gson.toJson(requestDto);
		OutputStream outputStream = null;
		PrintWriter printWriter = null;
		try {
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(outputStream,true);
			printWriter.println(reqJson);
			System.out.println("클라이언트 -> 서버: " + reqJson);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
}
