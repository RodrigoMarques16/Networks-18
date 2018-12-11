package rm.chat.client;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import rm.chat.shared.*;
import rm.chat.client.ChatListener;

public class ChatClient {

    // Variáveis relacionadas com a interface gráfica --- * NÃO MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das variáveis relacionadas coma interface gráfica

    // Se for necessário adicionar variáveis ao objecto ChatClient, devem
    // ser colocadas aqui
    private String server;
    private int port;
    private ChatListener listener;
    private SocketChannel channel;

    
    // Método a usar para acrescentar uma string à caixa de texto
    // * NÃO MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message);
    }

    
    // Construtor
    public ChatClient(String server, int port) throws IOException {

        // Inicialização da interface gráfica --- * NÃO MODIFICAR *
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                   chatBox.setText("");
                }
            }
        });
        // --- Fim da inicialização da interface gráfica

        // Se for necessário adicionar código de inicialização ao
        // construtor, deve ser colocado aqui
        this.server = server;
        this.port = port;
    }

    private void connectToServer() throws IOException{
        // Create a SocketChannel
        channel = SocketChannel.open();

        // Set it to non-blocking
        channel.configureBlocking(false);

        // Connect to the server
        InetSocketAddress isa = new InetSocketAddress(server, port);
        channel.connect(isa);

        // Wait for connection
        while(!channel.finishConnect()) {
            try {
                System.out.println("...");
                Thread.sleep(200);
            } catch (Exception e) {
                return;
            }
        }
        
        // Start listening for server messages
        //this.listener = new ChatListener();
        //listener.start();

        System.out.println("Connected to " + server + ":" + port);
    }


    // Método invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
        // PREENCHER AQUI com código que envia a mensagem ao servidor
    }
    
    // Método principal do objecto
    public void run() throws IOException {
        // PREENCHER AQUI
        connectToServer();
    }

    // Instancia o ChatClient e arranca-o invocando o seu método run()
    // * NÃO MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }   

}