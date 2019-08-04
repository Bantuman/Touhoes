import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.*; 

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

class Vector2
{           
    public float x;
    public float y;
    
    private float lerp(float a, float b, float f) 
    {
        return (a * (1f - f)) + (b * f);
    }
       
    public Vector2() {
        this.x = 0.0f;
        this.y = 0.0f;
    }
        
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2 Normalized()
    {
    	return this.multiply(1 / Length());
    }
    
    public float Length()
    {
    	return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
    
    public float Dot(Vector2 otherVector)
    {
    	return x * otherVector.x + y * otherVector.y;
    }
    
    public Vector2 subtract(Vector2 otherVector)
    {
    	return new Vector2(x - otherVector.x, y - otherVector.y);
    }
    
    public Vector2 add(Vector2 otherVector)
    {
    	return new Vector2(x + otherVector.x, y + otherVector.y);
    }
    
    public Vector2 multiply(float num)
    {
    	return new Vector2(x * num, y * num);
    }
    
    public Vector2 Lerp(Vector2 targetVector, float f)
    {
    	return new Vector2(lerp(x, targetVector.x, f), lerp(y, targetVector.y, f));
    }
    
    public Point ToPoint()
    {
    	return new Point((int)Math.round(this.x), (int)Math.round(this.y));
    }
    
    public Vector2 TranslatedBy(float dx, float dy)
    {
    	return new Vector2(this.x + dx, this.y + dy);
    }
    
    public Vector2 TranslatedBy(Vector2 dv)
    {
    	return new Vector2(this.x, this.y).TranslatedBy(dv.x, dv.y);
    }
    
    public void Translate(float dx, float dy)
    {
    	this.x += dx;
    	this.y += dy;
    }
    
    public void Translate(Vector2 dv)
    {
    	Translate(dv.x, dv.y);
    }
        
    public boolean equals(Vector2 other) {
        return (this.x == other.x && this.y == other.y);
    }
}



class Body{
	public static ArrayList<Body> allBodies;
	public static DebugRectangle DEBUG_RECT;
	
	public Rectangle bodyRectangle;
	public Vector2 bodyVelocity;
	public Vector2 bodyPosition;
	
	public Body(Vector2 bodyPosition, Rectangle bodySize)
	{
		this.bodyPosition = bodyPosition;
		this.bodyVelocity = new Vector2(0, 0);
		
		bodyRectangle = bodySize.getBounds();
		bodyRectangle.setLocation(bodyPosition.ToPoint());
		allBodies.add(this);
	}
	
	public Vector2 Update(float deltaTime)
	{
		return bodyPosition;
	}
}

class Animation{
	private Image currentImage;
	private BufferedImage imageBuffer;
	private Rectangle imageFrame;
	private float frameDelay,
				  frameTimer;
	
	private Point frameStart,
				  frameEnd;
	
	public Animation(BufferedImage imageBuffer, float frameDelay, Rectangle frameSize, Point frameStart, Point frameEnd)
	{
		this.imageBuffer = imageBuffer;
		this.frameDelay = frameDelay * 1000;
		this.imageFrame = frameSize.getBounds();
		this.frameStart = new Point(frameStart.x * (int)frameSize.getWidth(), frameStart.y * (int)frameSize.getHeight());
		this.frameEnd = new Point(frameEnd.x * (int)frameSize.getWidth(), frameEnd.y * (int)frameSize.getHeight());
		this.imageFrame.setLocation(this.frameStart);
		currentImage = imageBuffer.getSubimage(imageFrame.x, imageFrame.y, imageFrame.width, imageFrame.height);
	}
	
	public Image Update(float deltaTime)
	{
		frameTimer += deltaTime;
		if (frameTimer >= frameDelay)
		{
			frameTimer -= frameDelay;
			currentImage = imageBuffer.getSubimage(imageFrame.x, imageFrame.y, imageFrame.width, imageFrame.height);
			imageFrame.translate(imageFrame.width, 0);
			if (imageFrame.getLocation().x >= imageBuffer.getWidth())
			{
				imageFrame.setLocation(0, imageFrame.getLocation().y += imageFrame.height);
			}
			Point framePos = imageFrame.getLocation();
			if (framePos.x >= (frameEnd.x + imageFrame.width) && framePos.y >= frameEnd.y)
			{
				imageFrame.setLocation(frameStart);
			}
		}
		return currentImage;
	}
}
class FairyStomach extends TransferHandler {
	private Figure body;
	public int storedFood;
	public int maxFood;
	
	public FairyStomach(Figure body)
	{
		super();
		this.body = body;
		this.maxFood = body.stomachSize;
		this.storedFood = this.maxFood;
	}
	
	private void eatFile(File food) {
		long kb = (food.length() / 1024);
		storedFood += kb;
		food.delete();
		JFrameTesting.figureHandler.SendMessage("nom nom nom, current hunger: " + body.figureStomach.storedFood + " kb (+" + kb + ")", body.bodyRectangle.getLocation(), body.figureFrame.getTitle());
	}
	
	public boolean canImport(JComponent com, DataFlavor[] dataFlavors) {
		for (int i = 0; i < dataFlavors.length; i++) {
			DataFlavor flavor = dataFlavors[i];
			if (flavor.equals(DataFlavor.javaFileListFlavor)) {
				return true;
			}
			if (flavor.equals(DataFlavor.stringFlavor)) {
				return true;
			}
		}
		return false;
	}

	public boolean importData(JComponent comp, Transferable t) {
		DataFlavor[] flavors = t.getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
			try {
				if (flavor.equals(DataFlavor.javaFileListFlavor)) {   
					@SuppressWarnings("rawtypes")
					java.util.List l = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator iter = l.iterator();
					while (iter.hasNext()) {
						File file = (File) iter.next();
						eatFile(file);
					}
					return true;
				} else if (flavor.equals(DataFlavor.stringFlavor)) {
					String fileOrURL = (String) t.getTransferData(flavor);
					try {
						URL url = new URL(fileOrURL);
						return true;
					} catch (MalformedURLException ex) {
						return false;
					}
				} else {
				}
			} catch (IOException ex) {
			} catch (UnsupportedFlavorException e) {
			}
		}
		return false;
	}
}

class MessageBox extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MessageBox(String msg){
		super(msg);
	}
	public void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 15, 15);
		g.fillPolygon(new int[]{2, 8, 16}, new int[]{20, 28, 20}, 3);
		super.paintComponent(g);
  	}
}


class MessageHandle{
	
	DefaultListModel<String> messageLogNames = new DefaultListModel<>();  
	DefaultListModel<String> messageLogText = new DefaultListModel<>();  
	JList<String> messageLogListText;
	JList<String> messageLogListNames;
	JScrollPane messageLogScrollPane;
	
	public JFrame messageOverlayFrame;
	public JFrame messageLogFrame;
	
	public MessageHandle(){
		messageOverlayFrame = new JFrame("Message Overlay");
		messageOverlayFrame.setAlwaysOnTop(true);
		messageOverlayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		messageOverlayFrame.setType(javax.swing.JFrame.Type.UTILITY);
		messageOverlayFrame.setUndecorated(true);
		messageOverlayFrame.setBackground(new Color(1f, 1f, 1f, 0f));
		messageOverlayFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		messageOverlayFrame.setVisible(true);
		
		messageLogFrame = new JFrame("Touhoes");
		messageLogFrame.setResizable(false);
		messageLogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		messageLogFrame.setBounds(150, 150, 640, 420);
		messageLogFrame.setMaximumSize(new Dimension(420, 720));
		messageLogFrame.setMinimumSize(new Dimension(256, 64));
		
		JPanel messageLogPanel = new JPanel();
		messageLogPanel.setLayout(new FlowLayout());
        
		messageLogListNames = new JList<>(messageLogNames);
		messageLogListNames.setBounds(messageLogFrame.getBounds());
		messageLogPanel.add(messageLogListNames);
		
		messageLogListText = new JList<>(messageLogText);
		messageLogListText.setBackground(messageLogFrame.getBackground());
		messageLogListText.setFont(new Font("Dialog", Font.PLAIN, 12));
		messageLogListText.setBounds(messageLogFrame.getBounds());
		messageLogPanel.add(messageLogListText);
		
		DefaultListCellRenderer renderer = (DefaultListCellRenderer) messageLogListNames.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		renderer.setIcon(new ImageIcon(""));
		
		messageLogScrollPane = new JScrollPane(messageLogPanel);
		messageLogFrame.getContentPane().add(messageLogScrollPane);
		messageLogFrame.setVisible(true);
	}
	
	public void SendMessage(String message, Point location){
		SendMessage(message, location, "");
	}
	
	public void SendMessage(String message, Point location, String name){
		
		Font messageFont = new Font("Dialog", Font.BOLD, 12);
		MessageBox textLabel = new MessageBox(message);
		textLabel.setFont(messageFont);
		textLabel.setOpaque(false);
		textLabel.setBackground(new Color(1f, 1f, 1f, 0f));
		textLabel.setHorizontalAlignment(JLabel.CENTER);
		textLabel.setVerticalTextPosition(JLabel.TOP);
		textLabel.setVerticalAlignment(JLabel.CENTER);

		location.y -= 24;
		textLabel.setLocation(location);
		textLabel.setSize(messageOverlayFrame.getFontMetrics(messageFont).stringWidth(message) + 6, 28);
		messageOverlayFrame.getContentPane().add(textLabel);

		textLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            	textLabel.setEnabled(false);
            	messageOverlayFrame.getContentPane().remove(textLabel);
            	messageOverlayFrame.repaint();
            }
        });
		
		new Thread(){
		      @Override
		      public void run() {
		           try {
		                  Thread.sleep(3000);
		                  textLabel.setEnabled(false);
		              	  messageOverlayFrame.getContentPane().remove(textLabel);
		              	  messageOverlayFrame.repaint();

		           } catch (InterruptedException e) {
		                  e.printStackTrace();
		           }
		      };
		}.start();
		
		messageOverlayFrame.repaint();
		messageLogNames.addElement(name);
		messageLogText.addElement(message);
		messageLogScrollPane.getVerticalScrollBar().setValue(messageLogScrollPane.getVerticalScrollBar().getMaximum());
	}
}

class Figure extends Body{
	public static final float UPDATE_RATE = 1.5f;
	public static final float GRAVITY = 9.82f;
	public static ArrayList<Figure> allFigures;
	
	private Animation currentAnimation,
					  idleAnimation,
					  flyLeftAnimation,
					  flyRightAnimation,
					  flyCenterAnimation;
	
	private Vocabulary figureVocabulary;
	private float figureTalkTimer;
	private float figureTalkDelay;
	private ImageIcon imageIcon;
	private Random randomizer;
	private boolean figureGrounded;
	private boolean figureFlying;
	
	public FairyStomach figureStomach;
	public int stomachSize;
	public JFrame figureFrame;
	public float figureMass;
	public boolean figureDragging;
	public Point previousMousePosition;
	
	public void setVocabulary(Vocabulary someVocabulary)
	{
		figureVocabulary = someVocabulary;
	}
	
	public Figure(Vector2 figurePosition, String figureName, String imageName, Rectangle figureSize, float figureMass)
	{
		super(figurePosition, figureSize);
		this.figureMass = figureMass;
		this.bodyPosition = figurePosition;
		this.bodyVelocity = new Vector2(0, 0);
		this.bodyRectangle = figureSize;
		this.randomizer = new Random(figureName.length() + (int)figureMass);
		this.stomachSize = 5000;
		this.figureStomach = new FairyStomach(this);
		
		figureTalkDelay = (4 + this.randomizer.nextInt(50)) * 1000;
		bodyRectangle.setLocation(figurePosition.ToPoint());
		figureFrame = new JFrame(figureName);
		JComponent cp = (JComponent) figureFrame.getContentPane();
		cp.setTransferHandler(this.figureStomach);
		figureFrame.setAlwaysOnTop(true);
		figureFrame.setType(javax.swing.JFrame.Type.UTILITY);
		figureFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		figureFrame.setUndecorated(true);
		figureFrame.setBackground(new Color(1f, 1f, 1f, 0f));
		figureFrame.setBounds(figureSize);
		figureFrame.setLocation(bodyRectangle.getLocation());
		
		double scaleX = figureSize.getWidth() / 24;
		double scaleY = figureSize.getHeight() / 32;
		
		BufferedImage bufferedImage = JFrameTesting.LoadImage(imageName, scaleX, scaleY);
		imageIcon = new ImageIcon(bufferedImage);
		JLabel imageLabel = new JLabel(imageIcon);
		Rectangle scaledRectangle = new Rectangle((int)figureSize.getWidth(), (int)figureSize.getHeight());
		idleAnimation = new Animation(bufferedImage, 0.2f, scaledRectangle, new Point(0, 0), new Point(3, 0));
		flyRightAnimation = new Animation(bufferedImage, 0.5f, scaledRectangle, new Point(6, 2), new Point(6, 2));
		flyLeftAnimation = new Animation(bufferedImage, 0.5f, scaledRectangle, new Point(6, 6), new Point(6, 6));
		flyCenterAnimation = new Animation(bufferedImage, 0.5f, scaledRectangle, new Point(5, 0), new Point(5, 0));
		currentAnimation = idleAnimation;
		
		imageLabel.setPreferredSize(figureFrame.getSize());
		figureFrame.getContentPane().add(imageLabel, BorderLayout.CENTER);
		
		figureFrame.setLocationRelativeTo(null);
		figureFrame.pack();
		figureFrame.setVisible(true);
		allFigures.add(this);
		
		figureFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            	figureDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	if (e.isShiftDown())
            	{
            		figureFlying = !figureFlying;
            	}
            	figureDragging = false;
            }
        });
	}
	
	@Override
	public Vector2 Update(float deltaTime)
	{
		//m.Update(deltaTime);
		if (currentAnimation != null)
		{
			Image nextImage = currentAnimation.Update(deltaTime);
			if (nextImage != null)
			{
				imageIcon.setImage(nextImage);
				figureFrame.repaint();
			}
		}

		figureTalkTimer += deltaTime;
		if (figureTalkTimer >= figureTalkDelay)
		{
			figureTalkTimer = 0;
			figureTalkDelay = (4 + this.randomizer.nextInt(50)) * 1000;
			figureStomach.storedFood -= 20;
			if (figureStomach.storedFood >= figureStomach.maxFood / 2) {
				JFrameTesting.figureHandler.SendMessage((String)figureVocabulary.normal.get(randomizer.nextInt(figureVocabulary.normal.size())), bodyRectangle.getLocation(), figureFrame.getTitle());
			} else if (figureStomach.storedFood >= 100) {
				JFrameTesting.figureHandler.SendMessage((String)figureVocabulary.upset.get(randomizer.nextInt(figureVocabulary.upset.size())), bodyRectangle.getLocation(), figureFrame.getTitle());
			} else {
				JFrameTesting.figureHandler.SendMessage("Hunger is below 100 kilobytes, going berserk.", bodyRectangle.getLocation(), figureFrame.getTitle());

				// start eating random files
			}
		}
		
		float width = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		float height = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		if (bodyPosition.x < -128 || bodyPosition.x > width + 128 || bodyPosition.y < -64 || bodyPosition.y > height)
		{
			bodyPosition = new Vector2(100, 64);
			bodyVelocity = new Vector2(0, 0);
		}
		
		bodyVelocity = new Vector2(Math.min(5, Math.max(-5, bodyVelocity.x)), Math.min(5, Math.max(-5, bodyVelocity.y)));

		bodyVelocity.Translate(0, (GRAVITY / figureMass) * deltaTime * (figureFlying ? 0.01f : 1f));
		bodyPosition.Translate(bodyVelocity.x * deltaTime, bodyVelocity.y * deltaTime);
		figureGrounded = (bodyPosition.y > JFrameTesting.figureController.getHeight() - 96);
		if (bodyVelocity.x > 0.1f && !figureGrounded)
		{
			currentAnimation = flyRightAnimation;
		} else if(bodyVelocity.x < -0.1f && !figureGrounded)
		{
			currentAnimation = flyLeftAnimation;
		} else if(!figureGrounded)
		{
			currentAnimation = flyCenterAnimation;
		} else
		{
			currentAnimation = idleAnimation;
		}
		
		if (figureDragging)
		{
			Point mousePosition = MouseInfo.getPointerInfo().getLocation();
			if (mousePosition != null)
			{
				Point targetPosition = new Point(
						mousePosition.x + (int)Math.round(-bodyRectangle.getWidth() * 0.5f), 
						mousePosition.y + (int)Math.round(-bodyRectangle.getHeight() * 0.5f)
				);
				if (previousMousePosition == null)
				{
					previousMousePosition = targetPosition;
				}
				bodyPosition = bodyPosition.Lerp(new Vector2(targetPosition.x, targetPosition.y), (float)(1 - Math.pow(0.1, deltaTime)));
				bodyVelocity = bodyVelocity.Lerp(
						new Vector2((targetPosition.x - previousMousePosition.x) * 0.1f, (targetPosition.y - previousMousePosition.y) * 0.1f),
						(float)(1 - Math.pow(0.1, deltaTime))
				);
				previousMousePosition = targetPosition;
			}
		}
		
		return bodyPosition;
	}
}

class DebugRectangle extends JComponent {
	ArrayList<Body> bodies;
	
	public DebugRectangle(ArrayList<Body> bodies)
	{
		this.bodies = bodies;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(Body body : bodies)
		{
			Rectangle rectangle = body.bodyRectangle;
			g.drawRect (rectangle.getLocation().x, rectangle.getLocation().y, rectangle.width, rectangle.height); 
		}
	}
}

class Vocabulary {
	public JSONArray upset;
	public JSONArray normal;
	public JSONArray happy;
	public Vocabulary(JSONArray upset, JSONArray normal, JSONArray happy)
	{
		this.upset = upset;
		this.normal = normal;
		this.happy = happy;
	}
}

public class JFrameTesting {

	public static JFrame figureController;
	public static MessageHandle figureHandler;
	public static final boolean DEBUG = false;
	
	public static BufferedImage LoadImage(String path, double scaleX, double scaleY)
	{
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new File(path));
		} catch(IOException exception){
			System.out.println("Couldn't find image");
			exception.printStackTrace();
		}
		
		int w = bufferedImage.getWidth() * (int)scaleX;
		int h = bufferedImage.getHeight() * (int)scaleY;
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(bufferedImage, after);
		bufferedImage = after;
		return bufferedImage;
	}
	
	public static void main(String[] args) throws Exception  
    { 
		figureController = new JFrame("Controller");
		figureController.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		figureController.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		figureController.setFocusable(false);
		figureController.setFocusableWindowState(false);
		figureController.setUndecorated(true);
		figureController.setBackground(new Color(1f, 1f, 1f, 0f));
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
		Vector2 safeBounds = new Vector2(insets.left, insets.top);
		int width = gd.getDisplayMode().getWidth() - (insets.left + insets.right);
		int height = gd.getDisplayMode().getHeight() - (insets.top + insets.bottom); 
		
		Body.allBodies = new ArrayList<Body>();
		Figure.allFigures = new ArrayList<Figure>();
		Body.DEBUG_RECT = new DebugRectangle(Body.allBodies);
		
		figureHandler = new MessageHandle();
		
		File characterFolder = new File("figures/");
		for (final File f : characterFolder.listFiles()) {
			if (!f.isDirectory()) continue;
			
			// f.getAbsolutePath() 
			Object obj = new JSONParser().parse(new FileReader("figures/" + f.getName() + "/CharacterData.JSON")); 
	        JSONObject jo = (JSONObject) obj; 
	        
	        Map spawnPositionMap = (Map)jo.get("spawnPosition");
			Vector2 spawnPosition = new Vector2((long)spawnPositionMap.get("x"), (long)spawnPositionMap.get("y"));
			Map spriteSizeMap = (Map)jo.get("spriteSize");
			Point spriteSize = new Point((int)(long)spriteSizeMap.get("x"), (int)(long)spriteSizeMap.get("y"));
			String fullName = (String)jo.get("firstName") + " " + jo.get("lastName");
			int floatValue = (int)(long)jo.get("floatValue");
			
	        Map vocabulary = (Map)jo.get("vocabulary");
	        JSONArray upsetVocabulary = (JSONArray)vocabulary.get("upset");
	        JSONArray normalVocabulary = (JSONArray)vocabulary.get("normal");
	        JSONArray happyVocabulary = (JSONArray)vocabulary.get("happy");
			
	        Figure figure = new Figure(spawnPosition, fullName, "figures/" + f.getName() + "/CharacterSprite.png", new Rectangle(spriteSize.x, spriteSize.y), floatValue);
			figure.setVocabulary(new Vocabulary(upsetVocabulary, normalVocabulary, happyVocabulary));
		}
		
		new Body(new Vector2(0, height).add(safeBounds), new Rectangle(width, 51200));
		new Body(new Vector2(0, -51200).add(safeBounds), new Rectangle(width, 51200));
		new Body(new Vector2(width, -25600).add(safeBounds), new Rectangle(51200, height + 51200));
		new Body(new Vector2(-51200, -25600).add(safeBounds), new Rectangle(51200, height + 51200));		
		
		figureHandler.SendMessage(Figure.allFigures.size() + " characters spawned, but at what cost", new Point(width / 2, 50), "Shion Yorigami");
		
		if (DEBUG)
		{
			figureController.getContentPane().add(Body.DEBUG_RECT);
		}
	
		figureController.setLocation(0, 0);
		figureController.pack();
		figureController.setVisible(true);
		
		//List<File> dropppedFiles = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
		
		// MIGHT WANNA SWITCH TO THIS IN THE FUTURE: https://gafferongames.com/post/fix_your_timestep/
		
		boolean isRunning = true;
		long lastUpdate = System.currentTimeMillis();
		while(isRunning)
		{
			float deltaTime = System.currentTimeMillis() - lastUpdate;
			lastUpdate = System.currentTimeMillis();
			
			if (DEBUG)
			{
				Body.DEBUG_RECT.repaint();	
			}
			
			for(int i = 0; i < Body.allBodies.size(); ++i)
			{
				Body iBody = Body.allBodies.get(i);
				Vector2 oldLocation = iBody.bodyPosition;
				Vector2 oldVelocity = iBody.bodyVelocity;
				Vector2 newLocation = iBody.Update(deltaTime);
				
				Rectangle newRectangle = new Rectangle(iBody.bodyRectangle.getBounds());
				newRectangle.setLocation(newLocation.ToPoint());
				
				for (int j = i + 1; j < Body.allBodies.size(); ++j)
				{
					Body jBody = Body.allBodies.get(j);
					if (newRectangle.intersects(jBody.bodyRectangle) || newRectangle.contains(jBody.bodyRectangle))
					{
						if (iBody instanceof Figure && jBody instanceof Figure)
						{
							iBody.bodyPosition.Translate(iBody.bodyPosition.subtract(jBody.bodyPosition).Normalized());
							jBody.bodyPosition.Translate(jBody.bodyPosition.subtract(iBody.bodyPosition).Normalized());
							jBody.bodyVelocity = jBody.bodyVelocity.Lerp(new Vector2(0, 0), 0.1f);
							iBody.bodyVelocity = iBody.bodyVelocity.Lerp(new Vector2(0, 0), 0.1f);
						}
						else
						{
							Vector2 surfaceNormal = new Vector2(0, -1);
							Vector2 currentLocation = oldLocation.add(new Vector2(iBody.bodyRectangle.width * 0.5f, iBody.bodyRectangle.height * 0.5f));
							if (currentLocation.x < jBody.bodyPosition.x)
							{
								surfaceNormal = new Vector2(-1, 0);
							} 
							else if(currentLocation.x > jBody.bodyPosition.x + jBody.bodyRectangle.width)
							{
								surfaceNormal = new Vector2(1, 0);
							}
							if (currentLocation.y < jBody.bodyPosition.y)
							{
								surfaceNormal = new Vector2(0, -1);
							} 
							else if(currentLocation.y > jBody.bodyPosition.y + jBody.bodyRectangle.height)
							{
								surfaceNormal = new Vector2(0, 1);
							}
							
							Dimension intersection = newRectangle.intersection(jBody.bodyRectangle).getSize();
							oldLocation = newLocation.TranslatedBy(surfaceNormal.x * intersection.width, surfaceNormal.y * intersection.height);
							iBody.bodyPosition = oldLocation;
							if (oldVelocity.Length() > 0.4f)
							{
								float velocityX = Math.abs(iBody.bodyVelocity.x) * 0.5f;
								float velocityY = Math.abs(iBody.bodyVelocity.y) * 0.5f;
					
								Vector2 normalVector = oldVelocity.Normalized();
								Vector2 reflectedVector = (normalVector.subtract(surfaceNormal.multiply(2 * normalVector.Dot(surfaceNormal))));
								iBody.bodyVelocity = new Vector2(reflectedVector.x * velocityX, reflectedVector.y * velocityY);
							} else
							{
								iBody.bodyVelocity = new Vector2(0, 0);
							}
						}
					}
				}
				
				newRectangle.setLocation(iBody.bodyPosition.ToPoint());
				iBody.bodyRectangle = newRectangle;
				if (iBody instanceof Figure)
				{
					Figure iFigure = (Figure)iBody;
					iFigure.figureFrame.setLocation(iFigure.bodyRectangle.getLocation());
				}
			}
		}
	}
}
