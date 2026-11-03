import java.nio.file.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
public class Supplement268 {
 public static void main(String[] a)throws Exception{
  javafx.application.Platform.startup(()->{});javafx.application.Platform.setImplicitExit(false);
  try{
   Beta268.start();Beta268.cmd("list");
   Beta268.cmd("note 1 nt/SearchNeedle");Beta268.cmd("find nt/SearchNeedle");Beta268.cmd("view 1");Beta268.cmd("note 1 clear");
   Beta268.fx(()->{Beta268.check(Beta268.app.visible()==0,"#191 clear removes searched result");System.out.println("DISPOSITION #191 clear viewed="+(Beta268.app.viewed()==null?"null":Beta268.app.viewed().getName().fullName));return null;});
   Beta268.snapshot("regression191-clear.png");
   Beta268.cmd("list");Beta268.cmd("note 1 nt/SearchNeedle");Beta268.cmd("find nt/SearchNeedle");Beta268.cmd("view 1");Beta268.cmd("note 1 nt/Replaced");
   Beta268.fx(()->{Beta268.check(Beta268.app.visible()==0,"#191 replace removes searched result");System.out.println("DISPOSITION #191 replace viewed="+(Beta268.app.viewed()==null?"null":Beta268.app.viewed().getName().fullName));return null;});
   Beta268.cmd("list");Beta268.cmd("view 1");
   var before=Beta268.fx(()->Beta268.app.first().getActivity());
   Thread.sleep(1200);Beta268.cmd("note 1 nt/Replaced");Beta268.cmd("star 1");
   Beta268.fx(()->{Beta268.check(before.equals(Beta268.app.first().getActivity()),"repeat note and star preserve activity timestamps");return null;});
   Beta268.cmd("NOTE 1 nt/SYNTHETIC_VALID_PRIVATE268");
   Beta268.cmd("find nt/SYNTHETIC_VALID_PRIVATE268");
   Beta268.cmd("list");
   Beta268.cmd("note 1 nt/Reconnect after demo 😀 /backend");
   for(var h:java.util.logging.Logger.getLogger("").getHandlers())h.flush();
   Beta268.check(!Files.readString(Path.of("addressbook.log.0")).contains("SYNTHETIC_VALID_PRIVATE268"),"valid uppercase note and note search redacted");
   Beta268.fx(()->{var f=Beta268.field();Beta268.key(f,KeyCode.UP);String original=f.getText();f.setText("edited recalled draft");Beta268.key(f,KeyCode.DOWN);Beta268.key(f,KeyCode.UP);Beta268.check(f.getText().equals(original),"editing recalled text preserves stored entry");return null;});
   Beta268.cmd("help");Beta268.fx(()->{Beta268.stage.hide();Beta268.check(Window.getWindows().stream().noneMatch(Window::isShowing),"#242 hiding stage without close request hides Help");return null;});
   Beta268.fx(()->{Beta268.stage.show();return null;});
   Beta268.cmd("help");
   Beta268.fx(()->{Beta268.stage.fireEvent(new WindowEvent(Beta268.stage,WindowEvent.WINDOW_CLOSE_REQUEST));Beta268.check(Window.getWindows().stream().noneMatch(Window::isShowing),"close request alone closes main and Help");return null;});
   System.out.println("TOTAL CHECKS "+Beta268.checks);
  } finally {Beta268.stop();javafx.application.Platform.exit();}
 }
}
