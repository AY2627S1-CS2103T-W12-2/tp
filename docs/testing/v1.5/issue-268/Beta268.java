import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
import seedu.address.MainApp;
import seedu.address.model.person.Person;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Beta268 {
 static class Probe extends MainApp {
  Person first() { return model.getAddressBook().getPersonList().getFirst(); }
  int visible() { return model.getFilteredPersonList().size(); }
  Person viewed() { return model.getViewedPerson().get(); }
  void reload() throws Exception { check(model.getAddressBook().equals(storage.readAddressBook().orElseThrow()), "saved JSON equals model"); }
 }
 static Probe app; static Stage stage; static int checks;
 static <T> T fx(Callable<T> f) throws Exception { var t=new FutureTask<T>(f); Platform.runLater(t); return t.get(25,TimeUnit.SECONDS); }
 static void check(boolean b,String m) { if(!b)throw new AssertionError(m); checks++; System.out.println("PASS "+m); }
 static TextField field() {return (TextField)stage.getScene().lookup("#commandTextField");}
 static String result() {return ((TextArea)stage.getScene().lookup("#resultDisplay")).getText();}
 static void cmd(String s,boolean fail) throws Exception {fx(()->{field().setText(s); field().fireEvent(new ActionEvent()); check(field().getStyleClass().contains("error")==fail,"command "+(s.length()>95?s.substring(0,65)+"… ["+s.codePointCount(0,s.length())+" code points]":s)+" => "+result().replace('\n',' '));return null;});}
 static void cmd(String s)throws Exception{cmd(s,false);}
 static void key(javafx.scene.Node n,KeyCode c) {n.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",c,false,false,false,false));}
 static void expectNote(String s)throws Exception{fx(()->{check(app.first().getNote().value.equals(s),"note value ["+s.codePointCount(0,s.length())+"]"); return null;});}
 static void snapshot(String name)throws Exception{fx(()->{var root=stage.getScene().getRoot();root.applyCss();root.layout();var im=root.snapshot(null,null);int w=(int)im.getWidth(),h=(int)im.getHeight();var out=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);int[] a=new int[w*h];im.getPixelReader().getPixels(0,0,w,h,javafx.scene.image.PixelFormat.getIntArgbInstance(),a,0,w);out.setRGB(0,0,w,h,a,0,w);ImageIO.write(out,"png",Path.of(name).toFile());return null;});}
 static Stage help(){return (Stage)Window.getWindows().stream().filter(w->w!=stage&&w.isShowing()).findFirst().orElseThrow();}
 static void start()throws Exception{fx(()->{app=new Probe();app.init();stage=new Stage();app.start(stage);stage.setWidth(1280);stage.setHeight(720);System.out.println("DISPLAY outputScale="+stage.getOutputScaleX()+"x"+stage.getOutputScaleY()+" screen="+Screen.getPrimary().getBounds()+" dpi="+Screen.getPrimary().getDpi());return null;});}
 static void stop()throws Exception{fx(()->{app.stop();stage.hide();return null;});}
 public static void main(String[] args)throws Exception{
  Platform.startup(()->{});Platform.setImplicitExit(false);
  try{
   start();
   if(args.length>0&&args[0].equals("reload")){
    expectNote("Reconnect after demo 😀 /backend");
    fx(()->{check(app.first().isStarred(),"star survives separate-process reopen");app.reload();key(field(),KeyCode.UP);check(field().getText().isEmpty(),"recall resets on reopen");return null;});
    cmd("undo",true);cmd("help");fx(()->{check(help().isShowing(),"Help before window close");stage.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));return null;});
    // Programmatically firing CLOSE_REQUEST invokes app handlers; emulate native default close if unconsumed.
    fx(()->{stage.close();check(Window.getWindows().stream().noneMatch(Window::isShowing),"main window close hides Help");return null;});
    System.out.println("TOTAL CHECKS "+checks);return;
   }
   boolean old=args.length>0&&args[0].equals("old");
   if(old){fx(()->{check(app.first().getName().fullName.equals("Legacy Tester"),"legacy JSON loaded");check(app.first().getNote().isEmpty()&&!app.first().isStarred(),"missing note/star defaults");return null;});}
   else {cmd("clear");cmd("add n/Beta Tester p/91234567 e/beta@example.com t/test");cmd("add n/Other Tester p/92345678");}
   cmd("note 1 nt/  Met at booth / A; punctuation: !? 😀  ");expectNote("Met at booth / A; punctuation: !? 😀");
   cmd("note 1 nt/Replacement / slash 😀");expectNote("Replacement / slash 😀");
   cmd("note 1 nt/Replacement / slash 😀");
   cmd("undo");expectNote("Met at booth / A; punctuation: !? 😀");cmd("redo");expectNote("Replacement / slash 😀");
   cmd("note 1 clear");expectNote("");cmd("note 1 clear");cmd("undo");expectNote("Replacement / slash 😀");cmd("redo");expectNote("");
   cmd("note 1 nt/   ",true);expectNote("");
   cmd("note 1 nt/"+"a".repeat(1000));expectNote("a".repeat(1000));
   cmd("note 1 nt/"+"a".repeat(1001),true);expectNote("a".repeat(1000));
   cmd("note 1 nt/"+"😀".repeat(1000));expectNote("😀".repeat(1000));
   cmd("note 1 nt/"+"😀".repeat(1001),true);expectNote("😀".repeat(1000));
   cmd("view 1");
   fx(()->{stage.getScene().getRoot().applyCss();stage.getScene().getRoot().layout();var sc=(ScrollPane)stage.getScene().lookup(".detail-scroll");check(sc.getContent().getBoundsInLocal().getHeight()>sc.getViewportBounds().getHeight(),"long note exceeds viewport");sc.setVvalue(1);check(sc.getVvalue()==1,"long note scroll reaches bottom");return null;});
   snapshot("long-note.png");
   String sentinel="SYNTHETIC_PRIVATE_BETA268";
   for(String c:List.of("note 999 nt/"+sentinel,"NOTE nope nt/"+sentinel,"note 1 "+sentinel,"note 1 NT/"+sentinel,"note 1 nt/"+sentinel.repeat(60),"find NT/"+sentinel,"find nt /"+sentinel)){cmd(c,true);}
   cmd("star 1");cmd("star 1");cmd("undo");fx(()->{check(!app.first().isStarred(),"duplicate star creates no undo entry");return null;});cmd("redo");
   fx(()->{check(app.first().isStarred(),"redo star"); var detail=stage.getScene().lookup("#personDetailPanelPlaceholder");check(detail.lookup("#star").isVisible(),"detail star visible");var card=new seedu.address.ui.PersonCard(app.first(),1);check(card.getRoot().lookup("#star").isVisible(),"card star visible");return null;});
   cmd("filter starred");fx(()->{check(app.visible()==1,"starred filter count");return null;});
   cmd("unstar 1");fx(()->{check(app.visible()==0,"unstar removes contact from filter");check(app.viewed()==null,"unstar clears removed selection");return null;});
   cmd("unstar 1",true);cmd("undo");fx(()->{check(app.visible()==1&&app.first().isStarred(),"undo unstar restores filtered contact");return null;});cmd("redo");
   cmd("filter starred off");fx(()->{check(app.visible()==(old?1:2),"filter off restores contacts");return null;});
   cmd("unstar 1");cmd("unstar 1");
   fx(()->{var card=new seedu.address.ui.PersonCard(app.first(),1);check(!card.getRoot().lookup("#star").isVisible(),"unstarred card indicator hidden");return null;});
   cmd("view 1");fx(()->{check(!stage.getScene().lookup("#personDetailPanelPlaceholder").lookup("#star").isVisible(),"unstarred detail indicator hidden");return null;});
   cmd("list");cmd("find Beta");cmd("list");
   fx(()->{field().setText("unfinished draft");key(field(),KeyCode.UP);check(field().getText().equals("list"),"recall latest list");key(field(),KeyCode.UP);check(field().getText().equals("find Beta"),"list preserves prior recall");key(field(),KeyCode.DOWN);key(field(),KeyCode.DOWN);check(field().getText().equals("unfinished draft"),"Down restores unfinished draft");key(field(),KeyCode.DOWN);check(field().getText().equals("unfinished draft"),"newest limit preserves draft");return null;});
   cmd("invalidBetaCommand",true);cmd("invalidBetaCommand",true);
   fx(()->{field().clear();key(field(),KeyCode.UP);check(field().getText().equals("invalidBetaCommand"),"failed commands recalled");key(field(),KeyCode.UP);check(field().getText().equals("list"),"consecutive commands deduplicated");return null;});
   for(int i=0;i<51;i++)cmd("list "+i);
   fx(()->{field().setText("eviction draft");for(int i=0;i<55;i++)key(field(),KeyCode.UP);check(field().getText().equals("list 1"),"50-entry eviction and oldest boundary");for(int i=0;i<50;i++)key(field(),KeyCode.DOWN);check(field().getText().equals("eviction draft"),"history newest boundary after eviction");var out=(TextArea)stage.getScene().lookup("#resultDisplay");key(out,KeyCode.UP);check(field().getText().equals("eviction draft"),"Up outside command box leaves recall alone");key(out,KeyCode.DOWN);check(field().getText().equals("eviction draft"),"Down outside command box leaves recall alone");return null;});
   cmd("note 1 nt/Reconnect after demo 😀 /backend");cmd("star 1");cmd("view 1");cmd("filter starred");snapshot("journey.png");fx(()->{app.reload();return null;});
   cmd("help");fx(()->{var h=help();check(h.isShowing(),"help command opens window");var b=(Button)h.getScene().lookup("#copyButton");var clipboard=Clipboard.getSystemClipboard();var previous=new HashMap<DataFormat,Object>();for(var type:clipboard.getContentTypes())previous.put(type,clipboard.getContent(type));b.fire();check(clipboard.getString().equals(seedu.address.ui.HelpWindow.USERGUIDE_URL),"Copy URL exact guide link");clipboard.setContent(previous);h.hide();key(field(),KeyCode.F1);check(help().isShowing(),"F1 opens Help in command box");help().hide();var menu=(MenuBar)stage.getScene().lookup("#menuBar");menu.getMenus().stream().flatMap(m->m.getItems().stream()).filter(i->i.getText().contains("Help")).findFirst().orElseThrow().fire();check(help().isShowing(),"Help menu opens window");return null;});
   cmd("exit");fx(()->{check(stage.isShowing(),"exit result visible before close");return null;});Thread.sleep(1500);
   fx(()->{check(Window.getWindows().stream().noneMatch(Window::isShowing),"exit closes main and Help");return null;});
   for(var h:java.util.logging.Logger.getLogger("").getHandlers())h.flush();
   String logs=Files.readString(Path.of("addressbook.log.0"));check(!logs.contains(sentinel),"private synthetic note absent from logs");
   System.out.println("TOTAL CHECKS "+checks);
  }finally{if(app!=null)stop();Platform.exit();}
 }
}
