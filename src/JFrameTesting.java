import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

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

class Message extends JComponent{
	private String messageText;
	private Body messageAdornee;
	public JLabel messageLabel;
	private float messageTimer;
	private int messageIndex;
	private float messageDelay;
	
	public Message(String text, float timeInbetweenWords, Vector2 position, Body body)
	{
		this.messageDelay = timeInbetweenWords * 1000;
		this.messageText = text;
		this.messageAdornee = body;
	}
	
	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString(messageText.substring(0, messageIndex), (int)Math.round(messageAdornee.bodyPosition.x), (int)Math.round(messageAdornee.bodyPosition.y));
    }
	
	public void Update(float deltaTime)
	{
		messageTimer += deltaTime;
		if (messageTimer >= messageDelay)
		{
			messageTimer -= messageDelay;
			
			if (messageIndex < messageText.length())
			{
				++messageIndex;
				repaint();
			}
		}
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
class MyFileTransferHandler extends TransferHandler {
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
          java.util.List l = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
		  Iterator iter = l.iterator();
          while (iter.hasNext()) {
            File file = (File) iter.next();
            // nom nom nom
            file.delete();
            System.out.println("ate file " + file.getCanonicalPath());
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
class Figure extends Body{
	public static final float UPDATE_RATE = 1.5f;
	public static final float GRAVITY = 9.82f;
	public static ArrayList<Figure> allFigures;
	
	private Message m;
	private Animation currentAnimation,
					  idleAnimation,
					  flyLeftAnimation,
					  flyRightAnimation,
					  flyCenterAnimation;
	
	private ImageIcon imageIcon;
	private boolean figureGrounded;
	private boolean figureFlying;
	
	public JFrame figureFrame;
	public float figureMass;
	public boolean figureDragging;
	public Point previousMousePosition;
	
	public Figure(Vector2 figurePosition, String figureName, String imageName, Rectangle figureSize, float figureMass)
	{
		super(figurePosition, figureSize);
		this.figureMass = figureMass;
		this.bodyPosition = figurePosition;
		this.bodyVelocity = new Vector2(0, 0);
		this.bodyRectangle = figureSize;
		
		bodyRectangle.setLocation(figurePosition.ToPoint());
		figureFrame = new JFrame(figureName);
		JComponent cp = (JComponent) figureFrame.getContentPane();
		cp.setTransferHandler(new MyFileTransferHandler());
		figureFrame.setAlwaysOnTop(true);
		figureFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		figureFrame.setUndecorated(true);
		figureFrame.setBackground(new Color(1f, 1f, 1f, 0f));
		figureFrame.setBounds(figureSize);
		figureFrame.setLocation(bodyRectangle.getLocation());
		
		//m = new Message("testing 123 lol ololol", 0.1f, bodyPosition, this);
		//JFrameTesting.figureController.getContentPane().add(m);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new File(imageName));
		} catch(IOException exception){
			System.out.println("Couldn't find image");
			exception.printStackTrace();
		}
		
		double scaleX = figureSize.getWidth() / 24;
		double scaleY = figureSize.getHeight() / 32;
		int w = bufferedImage.getWidth() * (int)scaleX;
		int h = bufferedImage.getHeight() * (int)scaleY;
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(bufferedImage, after);
		bufferedImage = after;
		
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

public class JFrameTesting {

	public static JFrame figureController;
	public static final boolean DEBUG = false;
	public static void main(String[] args) {
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
		
		new Figure(new Vector2(50, 0), "Sanae Kochiya", "figures/Sanae Kochiya.png", new Rectangle(24 * 2, 32 * 2), 4000);
		new Figure(new Vector2(150, 0), "Cirno", "figures/Cirno.png", new Rectangle(24 * 2, 32 * 2), 7000);
		new Figure(new Vector2(250, 0), "Youmu Konpaku", "figures/Youmu Konpaku.png", new Rectangle(24 * 2, 32 * 2), 6000);
		new Figure(new Vector2(350, 0), "Reimu Hakurei", "figures/Reimu Hakurei.png", new Rectangle(24 * 2, 32 * 2), 5000);
		new Figure(new Vector2(450, 0), "Alice Margatroid", "figures/Alice Margatroid.png", new Rectangle(24 * 2, 32 * 2), 4000);
		new Figure(new Vector2(550, 0), "Flandre Scarlet", "figures/Flandre Scarlet.png", new Rectangle(24 * 2, 32 * 2), 5000);
		new Figure(new Vector2(650, 0), "Sakuya Izayoi", "figures/Sakuya Izayoi.png", new Rectangle(24 * 2, 32 * 2), 5000);
		new Figure(new Vector2(750, 0), "Hong Mieling", "figures/Hong Meiling.png", new Rectangle(24 * 2, 32 * 2), 5000);
		
		new Body(new Vector2(0, height).add(safeBounds), new Rectangle(width, 5120));
		new Body(new Vector2(0, -5120).add(safeBounds), new Rectangle(width, 5120));
		new Body(new Vector2(width, -2560).add(safeBounds), new Rectangle(51200, height + 5120));
		new Body(new Vector2(-51200, -2560).add(safeBounds), new Rectangle(51200, height + 5120));		
		
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
