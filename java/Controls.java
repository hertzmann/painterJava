import java.awt.*;
import java.awt.event.*;

final class Controls extends Panel implements ActionListener
{
  TextField threshTF;

  TextField smallBrushTF;
  TextField numLayersTF;

  TextField gridFacTF;
  Checkbox randCB;
  TextField aaTF;
  TextField lengthTF;

  Checkbox underCB;
  Checkbox clipCB;
  Checkbox ueCB;
  TextField clipTF;

  TextField blurTF;

  Checkbox nldCB;
  Checkbox luvCB;
  Checkbox animCB;
  //  Checkbox aveCB;

  TextField filterFacTF;

  TextField minLenTF;
  TextField maxLenTF;

  TextField hfacTF;
  TextField hjitTF;

  TextField sfacTF;
  TextField sjitTF;

  TextField bfacTF;
  TextField bjitTF;

  TextField RGBjitTF;

  TextField opTF;

  Painter ai;

  TextField gradFacTF;

  Controls(Painter ai)
  {
    threshTF = new TextField(ai.threshold+"");
    smallBrushTF = new TextField(ai.minBrushSize+"");
    numLayersTF = new TextField(ai.numLayers+"");
    gridFacTF = new TextField(ai.gridFac+"");
    randCB = new Checkbox("Rand",ai.rand);
    aaTF = new TextField(ai.aa+"");
    lengthTF = new TextField(ai.lengthFac+"");

    hjitTF = new TextField(ai.hjit+"");
    hfacTF = new TextField(ai.hfac+"");
    
    sjitTF = new TextField(ai.sjit+"");
    sfacTF = new TextField(ai.sfac+"");
    
    bjitTF = new TextField(ai.bjit+"");
    bfacTF = new TextField(ai.bfac+"");

    RGBjitTF = new TextField(ai.RGBjit+"");
    
    blurTF = new TextField(ai.blurFac+"");
    nldCB = new Checkbox("NLD",ai.nonLinearDiffusion);
    luvCB = new Checkbox("LUV",ai.luvDistance);
    animCB = new Checkbox("Animate",ai.animate);
    //    aveCB = new Checkbox("AveColor",ai.colorAverage);
    minLenTF = new TextField(ai.minLength+"");
    maxLenTF = new TextField(ai.maxLength+"");

    filterFacTF = new TextField(ai.filterFac+"");

    underCB = new Checkbox("Underpaint",ai.underpaint);
    clipCB = new Checkbox("Clip",ai.clip);
    ueCB = new Checkbox("UE",ai.updateError);
    clipTF = new TextField(ai.clipFac+"");

    opTF = new TextField(ai.opacity+"");

    gradFacTF = new TextField(ai.gradFac+"");

    setLayout(new BorderLayout());

    Panel p = new Panel();
    
    add("North",p);

    p.setLayout(new FlowLayout());

    p.add(new Label("Threshold:"));
    p.add(threshTF);
    
    p.add(new Label(" Smallest Brush Size:"));
    p.add(smallBrushTF);
    
    p.add(new Label(" Num Layers:"));
    p.add(numLayersTF);


    p.add(new Label("Opacity:"));
    p.add(opTF);

    p = new Panel();
    add("Center",p);

    p.add(new Label("gridFac:"));
    p.add(gridFacTF);

    p.add(randCB);

    p.add(new Label("AA:"));
    p.add(aaTF);

    p.add(new Label("LF:"));
    p.add(lengthTF);

    p.add(new Label("Blur:"));
    p.add(blurTF);

    p.add(underCB);
    p.add(clipCB);
    p.add(ueCB);

    p.add(new Label("CF:"));
    p.add(clipTF);

    p = new Panel();
    add("South",p);

    Panel p1 = new Panel();

    p.add(p1);

    p1.setLayout(new BorderLayout());

    p = new Panel();
    p1.add("North",p);

    p.add(nldCB);
    p.add(luvCB);
    p.add(animCB);
    //    p.add(aveCB);
    p.add(new Label("MinLen:"));
    p.add(minLenTF);
    p.add(new Label("MaxLen:"));
    p.add(maxLenTF);

    p.add(new Label("FF:"));
    p.add(filterFacTF);

    p.add(new Label("GF:"));
    p.add(gradFacTF);

    p.add(new Label("RGBjit:"));
    p.add(RGBjitTF);

    p = new Panel();
    p1.add("Center",p);
    
    p.add(new Label("hfac:"));
    p.add(hfacTF);

    p.add(new Label("hjit:"));
    p.add(hjitTF);

    p.add(new Label("sfac:"));
    p.add(sfacTF);

    p.add(new Label("sjit:"));
    p.add(sjitTF);

    p.add(new Label("bfac:"));
    p.add(bfacTF);

    p.add(new Label("bjit:"));
    p.add(bjitTF);

    Button b = new Button("Go!");

    p.add(b);

    b.addActionListener(this);

    //    enableEvents(AWTEvent.ACTION_EVENT_MASK);
    this.ai = ai;
  }

  public void actionPerformed(ActionEvent e)
  {
    ai.threshold = Integer.parseInt(threshTF.getText());
    ai.minBrushSize = Integer.parseInt(smallBrushTF.getText());
    ai.numLayers = Integer.parseInt(numLayersTF.getText());

    ai.opacity = new Double(opTF.getText()).doubleValue();

    ai.gridFac = new Double(gridFacTF.getText()).doubleValue();
    ai.rand = randCB.getState();
    ai.aa = new Double(aaTF.getText()).doubleValue();
    ai.lengthFac = new Double(lengthTF.getText()).doubleValue();

    ai.blurFac = new Double(blurTF.getText()).doubleValue();

    ai.nonLinearDiffusion = nldCB.getState();
    ai.luvDistance = luvCB.getState();
    ai.animate = animCB.getState();
    //    ai.colorAverage = aveCB.getState();

    ai.minLength = Integer.parseInt(minLenTF.getText());
    ai.maxLength = Integer.parseInt(maxLenTF.getText());

    ai.hfac = new Double(hfacTF.getText()).doubleValue();
    ai.hjit = new Double(hjitTF.getText()).doubleValue();

    ai.sfac = new Double(sfacTF.getText()).doubleValue();
    ai.sjit = new Double(sjitTF.getText()).doubleValue();

    ai.bfac = new Double(bfacTF.getText()).doubleValue();
    ai.bjit = new Double(bjitTF.getText()).doubleValue();

    ai.RGBjit = new Double(RGBjitTF.getText()).doubleValue();

    ai.gradFac = new Double(gradFacTF.getText()).doubleValue();

    ai.underpaint = underCB.getState();
    ai.clip = clipCB.getState();
    ai.updateError = ueCB.getState();
    ai.clipFac = new Double(clipTF.getText()).doubleValue();

    ai.filterFac = new Double(filterFacTF.getText()).doubleValue();

    ai.makeStrokes();

    Toolkit.getDefaultToolkit().beep();
  }
}
