package SecureChatClient;

import SecureChatClient.Command.Authorization;
import SecureChatClient.Command.Command;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Controller implements AutoCloseable{

    private Channel ch;
    private BufferedReader readerConsole;
    private static final int lengthPassword = 3;

    public Controller(Channel ch) {
        this.ch = ch;


    }

    public void work() throws Exception {
        if (readerConsole==null) {
            readerConsole = new BufferedReader(new InputStreamReader(System.in));
        }
        while (true){
            String commandLine = readerConsole.readLine();

            if (commandLine.equals("exit")){
                this.close();
            }

            if (commandLine.startsWith("newUser")){
                commandLine=null;
                autorization(Command.newUser);
            }

            if (commandLine.startsWith("user")){
                commandLine=null;
                autorization(Command.user);
            }

        }
    }

    public void autorization(Command instr) throws IOException {

        String name = new String();
        while (true){
            System.out.print("Введите \"Имя пользователя\":");
            name = readerConsole.readLine();
            if (!name.equals("") & name.indexOf(" ")==-1) break;
            System.out.printf("%s%n",(name.equals(""))?
                    "Поле \"Имя пользователя\" не может быть пустым!":
                    "Поле \"Имя пользователя\" не должно содержать пробелы!");
            System.out.print("Введите \"Имя пользователя\":");
        }

        String password = new String();

        while (true){
            System.out.print("Пароль:");
            password = readerConsole.readLine();
            boolean hasNumber = false;
            boolean hasLowerCase = false;
            boolean hasUpperCase = false;
            boolean hasNotGap = (password.indexOf(" ") == -1);
            if (!password.equals("")){
                for (int i = 0; i < password.length(); i++) {
                    if (!hasNumber) {
                        hasNumber = Pattern.matches("\\p{N}", String.valueOf(password.charAt(i)));
                    }
                    if (!hasLowerCase){
                        hasLowerCase = Pattern.matches("\\p{Ll}",String.valueOf(password.charAt(i)));
                    }
                    if (!hasUpperCase){
                        hasUpperCase = Pattern.matches("\\p{Lu}",String.valueOf(password.charAt(i)));
                    }
                }

                if (password.length()>lengthPassword-1
                & hasNumber
                & hasLowerCase
                & hasUpperCase
                & hasNotGap){break;}

                if (password.length()<lengthPassword) System.out.printf("Длительность пароля должна быть больше %d символов%n",lengthPassword);
                if (!hasNumber) System.out.println("В пароле должен быть как минимум 1 цифра");
                if (!hasLowerCase) System.out.println("В пароле должен быть символ в нижнем регистре");
                if (!hasUpperCase) System.out.println("В пароле должен быть символ в верхнем регистре");
                if (!hasNotGap) System.out.println("В пароле не должно быть разделительных символов");
            }

        }

        Authorization command = new Authorization(instr);
        command.setNickName(name);
        command.setPassword(password);
//Тест вывод на экран
        for (String s : command.toJSON()) {
            System.out.print(s);
        }

        ch.writeAndFlush(command.objToByte());

        while (ch.read()==null){}
        

    }


    @Override
    public void close() throws Exception {
        ch.flush();
        ch.close();
        readerConsole.close();

    }


}
