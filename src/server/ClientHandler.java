package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;


    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
this.nick = "";
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");

                            System.out.println(str);
                            if (token.length < 2) {
                                continue;
                            }

                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);

                            if (newNick != null) {
                                sendMsg("/authok " + newNick);
                                this.nick = newNick;
                                login = token[1];
                                server.subscribe(this);
                                System.out.println("Клиент: " + this.nick + " подключился");
                                break;
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();


                        if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }
                    else if (str.startsWith("/w ")) {
                        String[] elements = str.split(" ", 3);

                        server.broadcastMsg("Сообщение от " + this.nick + " для " + elements[1] + " (приват): " + elements[2], this.nick, elements[1]);
                    } else {   server.broadcastMsg(this.nick + ": " + str);
                    }}
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String msg) {
        try {
            System.out.println("->сообщение" + (this.nick != null ? " " + this.nick : "") + ": " + msg);

            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getName() {
        return nick;
    }
}
