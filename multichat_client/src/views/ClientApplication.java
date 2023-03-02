package views;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
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
import lombok.Getter;
import lombok.Setter;

@Getter
public class ClientApplication extends JFrame {
	
	private static ClientApplication instance;
	
	private Gson gson;
	private Socket socket;
	
	private JPanel mainPanel;
	private CardLayout maincard;
	
	private JTextField usernameField;
	
	private JTextField sendMessageField;
	
	@Setter
	private List<Map<String, String>> roomInfoList;
	private DefaultListModel<String> roomNameListModel;
	private DefaultListModel<String> usernameListModel;	
	private JList roomList;
	private JList joinUserList;
	
	private JTextArea chattingContent;
	
	public static ClientApplication getInstance(){
		if(instance == null) {
			instance = new ClientApplication();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientApplication frame = ClientApplication.getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		});
	}

	private ClientApplication() {
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				RequestDto<String> requestDto = new RequestDto<String>("exitRoom", null);
				sendRquest(requestDto);
			}
		});
			
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
		} finally {
			System.out.println("클라이언트 종료");
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
					RequestDto<String> usernameCheckReqDto = 
							new RequestDto<String>("usernameCheck", usernameField.getText());
					sendRquest(usernameCheckReqDto);
//					enterButton.doClick();
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
		
		roomNameListModel = new DefaultListModel<String>() ;
		roomList = new JList(roomNameListModel);
		roomList.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					int selectIndex = roomList.getSelectedIndex();
					RequestDto<Map<String,String>> requestDto = 
							new RequestDto<Map<String,String>>("enterRoom", roomInfoList.get(selectIndex));
					sendRquest(requestDto);
				}
			}
		});
		roomListScroll.setViewportView(roomList);
		
		JButton createButton = new JButton("방 생성");
		createButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String roomName = null;
				while(true) {
					roomName = JOptionPane.showInputDialog(null,"생성할 방의 제목을 입력하세요", "방생성", JOptionPane.PLAIN_MESSAGE);
					if(roomName == null) {
						return;
					}
					if(!roomName.isBlank()) {
						break;
					}
					JOptionPane.showMessageDialog(null,"공백은 사용할 수 없습니다.", "방생성 오류", JOptionPane.ERROR_MESSAGE);
				}
				
				RequestDto<String> requestDto = new RequestDto<String>("createRoom", roomName);
				sendRquest(requestDto);
			}
		});
		createButton.setBounds(12, 10, 74, 78);
		roomListPanel.add(createButton);
		
		/*==================<< Room Panel >>==================*/
		
		JScrollPane joinUserListScroll = new JScrollPane();
		joinUserListScroll.setBounds(0, 0, 348, 103);
		roomPanel.add(joinUserListScroll);
		
		usernameListModel = new DefaultListModel<String>();
		joinUserList = new JList(usernameListModel);
		joinUserListScroll.setViewportView(joinUserList);
		
		JButton roomExitButton = new JButton("나가기");
		roomExitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(JOptionPane.showConfirmDialog(null, "방을 나가시겠습니까?", "방 나가기", JOptionPane.YES_NO_OPTION) == 0) {
					RequestDto<String> requestDto = new RequestDto<String>("exitRoom", null);
					sendRquest(requestDto);
				} 
			}
		});
		roomExitButton.setBounds(348, 0, 116, 103);
		roomPanel.add(roomExitButton);
		
		JScrollPane chattingContentsScroll = new JScrollPane();
		chattingContentsScroll.setBounds(0, 103, 464, 584);
		roomPanel.add(chattingContentsScroll);
		
		chattingContent = new JTextArea();
		chattingContentsScroll.setViewportView(chattingContent);
		chattingContent.setEditable(false);
		
		sendMessageField = new JTextField();
		sendMessageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					RequestDto<String> requestDto = new RequestDto<String>("sendMessage", sendMessageField.getText());
					sendMessageField.setText("");
					sendRquest(requestDto);
				}
			}
		});
		sendMessageField.setBounds(0, 690, 388, 71);
		roomPanel.add(sendMessageField);
		sendMessageField.setColumns(10);
		
		JButton sendButton = new JButton("전송");
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> requestDto = new RequestDto<String>("sendMessage", sendMessageField.getText());
				sendMessageField.setText("");
				sendRquest(requestDto);
			}
		});
		sendButton.setBounds(388, 689, 76, 72);
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
