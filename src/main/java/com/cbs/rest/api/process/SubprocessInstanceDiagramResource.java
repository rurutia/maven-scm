package com.cbs.rest.api.process;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.runtime.process.BaseProcessInstanceResource;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

import com.cbs.rest.api.utility.Utility;

public class SubprocessInstanceDiagramResource extends SecuredResource {
	protected String activityFontName = "Arial";
	protected FontMetrics fontMetrics;
	protected String labelFontName = "Arial";
	protected static final int FONT_SIZE = 16;
	protected static Stroke END_EVENT_STROKE = new BasicStroke(3.0f);
	protected Graphics2D g;
	protected static Color LABEL_COLOR = new Color(112, 146, 150);
	protected static Color LABEL_COLOR_HIGHLIGHT = new Color(255, 200, 0);
	
	protected Font LABEL_FONT = new Font(labelFontName, Font.BOLD, 16);
    protected static final int ARROW_WIDTH = 5;
	  
	private String subprocessId;
	  
	private void traverseProcessTree(String superProcessInstanceId, String superProcessTreeId, List<String> processTree) {
		List<HistoricProcessInstance> subprocesses = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
																.superProcessInstanceId(superProcessInstanceId)
																.list();
		
		for (HistoricProcessInstance processInstance : subprocesses) {
			String processId = processInstance.getId();
			String processDefinitionName = Utility.getProcessDefinitionNameFromDefinitionId(processInstance.getProcessDefinitionId());
			String processTreeId = superProcessTreeId + ">>" + processDefinitionName + "," + processId;
			traverseProcessTree(processId, processTreeId, processTree);
		    
			processTree.add(0, processTreeId);
		}
	}

	@SuppressWarnings("unchecked")
	@Get
	public InputRepresentation getProcessInstanceDiagram() {
		if(authenticate() == false) return null;
		
		String processId = getQuery().getValues("processId");
		
		if(processId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id provided");
		}
		
		subprocessId = getQuery().getValues("subprocessId");
		
		if(subprocessId == null) {
			subprocessId = "subprocessId not provided";
		}

		HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
													.processInstanceId(processId)
													.singleResult();

		if (processInstance == null) {
			throw new ActivitiObjectNotFoundException("Process instance with id" + processId + " could not be found", ProcessInstance.class);
		}
		
		BufferedImage processDiagram;

		processDiagram = new BufferedImage(1600, 1200, BufferedImage.TYPE_INT_ARGB);
		g = processDiagram.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(Color.black);

		Font font = new Font(activityFontName, Font.BOLD, FONT_SIZE);
		g.setFont(font);
		this.fontMetrics = g.getFontMetrics();

		List<String> processTree = new ArrayList<String>();
		
		String processDefinitionName = Utility.getProcessDefinitionNameFromDefinitionId(processInstance.getProcessDefinitionId());
		
		traverseProcessTree(processId, processDefinitionName + "," + processId, processTree);
		
		processTree.add(0, processDefinitionName + "," + processId);

		int DEFAULT_START_X = 15;
		int DEFAULT_START_Y = 15;
		int DEFAULT_GAP_X = 350;
		int DEFAULT_GAP_Y = 80;
		
		int level = 1;
		int startX =  DEFAULT_START_X;
		int startY = DEFAULT_START_Y;

		Map<String, Object> nodesSameLevel;
		
		drawLabel(processTree.get(0), DEFAULT_START_X, DEFAULT_START_Y); 
		
		int previousX =  DEFAULT_START_X + fontMetrics.stringWidth(processTree.get(0).split(",")[0]);
		int previousY = DEFAULT_START_Y;
		
		processTree.remove(0);
		
		Map<Integer, Map<String, Object>> model = new HashMap<Integer, Map<String, Object>>();
		nodesSameLevel = new HashMap<String, Object>();
		nodesSameLevel.put("previousX", previousX);
		nodesSameLevel.put("previousY", previousY);
		model.put(level, nodesSameLevel);
		
		for(String process : processTree) {
			level = process.split(">>").length;
			String processDetail = process.split(">>")[level-1];
			if(model.get(level) == null) {
				nodesSameLevel = new HashMap<String, Object>();
				nodesSameLevel.put("nodes", new ArrayList<String>());
				((ArrayList<String>)nodesSameLevel.get("nodes")).add(processDetail);

				startX = (level - 1) * DEFAULT_GAP_X + DEFAULT_START_X;
				startY = DEFAULT_START_Y;
				drawLabel(processDetail, startX, startY);
			}
			else {
				nodesSameLevel = model.get(level);
				((ArrayList<String>)nodesSameLevel.get("nodes")).add(processDetail);
				startX = (level - 1) * DEFAULT_GAP_X + DEFAULT_START_X;
				startY += DEFAULT_GAP_Y;
				drawLabel(processDetail, startX, startY);
			}
			drawSequence(model, level, previousX, previousY, nodesSameLevel, processDetail, startX, startY);

		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedImage imageToSerialize = processDiagram;

		try {
			ImageIO.write(imageToSerialize, "png", out);
		} catch (IOException e) {
			throw new ActivitiException("Error while generating process image", e);
		} finally {
			IoUtil.closeSilently(out);
		}
		InputStream resource = new ByteArrayInputStream(out.toByteArray());

		InputRepresentation output = new InputRepresentation(resource, MediaType.IMAGE_PNG);
		return output;
	}
	
	private void drawSequence(Map<Integer, Map<String, Object>> model, int level, int previousX, int previousY, Map<String, Object> nodesSameLevel, String processDetail, int startX, int startY) {
		nodesSameLevel.put("previousX", startX + fontMetrics.stringWidth(processDetail.split(",")[0]));
		nodesSameLevel.put("previousY", startY);
		
		model.put(level, nodesSameLevel);
		
		Map<String, Object> nodesPreviousLevel = model.get(level - 1);
		previousX = (Integer)nodesPreviousLevel.get("previousX");
		previousY = (Integer)nodesPreviousLevel.get("previousY");
		Line2D.Double line = new Line2D.Double(
	    		previousX, 
	    		previousY, 
	    		startX,
	    		startY);
	    drawArrowHead(line);
	    g.draw(line);
	}
	
	  public void drawArrowHead(Line2D.Double line) {
		    int doubleArrowWidth = 2 * ARROW_WIDTH;
		    Polygon arrowHead = new Polygon();
		    arrowHead.addPoint(0, 0);
		    arrowHead.addPoint(-ARROW_WIDTH, -doubleArrowWidth);
		    arrowHead.addPoint(ARROW_WIDTH, -doubleArrowWidth);

		    AffineTransform transformation = new AffineTransform();
		    transformation.setToIdentity();
		    double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
		    transformation.translate(line.x2, line.y2);
		    transformation.rotate((angle - Math.PI / 2d));

		    AffineTransform originalTransformation = g.getTransform();
		    g.setTransform(transformation);
		    g.fill(arrowHead);
		    g.setTransform(originalTransformation);
		  }

	public void drawLabel(String text, int x, int y){
		// text
		if (text != null) {
			Paint originalPaint = g.getPaint();
			Font originalFont = g.getFont();
			
			int textX = x ;
			int textY = y ;

			String processName = text.split(",")[0];
			String processId = text.split(",")[1];
			
			if(text.contains(subprocessId)) {
				g.setPaint(LABEL_COLOR_HIGHLIGHT);
				g.fillRect(textX - 2, textY - fontMetrics.getHeight()+ 4, fontMetrics.stringWidth(processName), fontMetrics.getHeight()*2);
			}

			g.setPaint(LABEL_COLOR);
			g.setFont(LABEL_FONT);

			g.drawString(processName, textX, textY);
			g.drawString(processId, textX, textY + fontMetrics.getHeight());
			
			g.drawRect(textX - 2, textY - fontMetrics.getHeight()+ 4, fontMetrics.stringWidth(processName), fontMetrics.getHeight()*2);
			
			
			// restore originals
			g.setFont(originalFont);
			g.setPaint(originalPaint);
		}
	}

}
