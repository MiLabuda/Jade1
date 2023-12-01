package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class MyAgent extends Agent {
	// HashMap to store the correspondence between word and translation
	HashMap<String, String> translationMap = new HashMap<>();

	protected void setup () {
		displayResponse("Hello, I am " + getAID().getLocalName());
		addBehaviour(new MyCyclicBehaviour(this));
		//doDelete();
	}
	protected void takeDown() {
		displayResponse("See you");
	}
	public void displayResponse(String message) {
		JOptionPane.showMessageDialog(null,message,"Message",JOptionPane.PLAIN_MESSAGE);
	}

	void displayTranslationHtml(String originalWord, String translation) {
		// Format the translation information as HTML
		String htmlContent = "<html><body>" +
				"<p><b>Original Word:</b> " + originalWord + "</p>" +
				"<p><b>Translation:</b> " + translation + "</p>" +
				"<hr></body></html>";

		// Display the HTML content
		displayHtmlResponse(htmlContent);
	}

	public void displayHtmlResponse(String html) {
		JTextPane tp = new JTextPane();
		JScrollPane js = new JScrollPane();
		js.getViewport().add(tp);
		JFrame jf = new JFrame();
		jf.getContentPane().add(js);
		jf.pack();
		jf.setSize(400,500);
		jf.setVisible(true);
		tp.setContentType("text/html");
		tp.setEditable(false);
		tp.setText(html);
	}
}

class MyCyclicBehaviour extends CyclicBehaviour {

	MyAgent myAgent;
	public MyCyclicBehaviour(MyAgent myAgent) {
		this.myAgent = myAgent;
	}
	public void action() {
		ACLMessage message = myAgent.receive();
		if (message == null) {
			block();
		} else {
			String ontology = message.getOntology();
			String content = message.getContent();
			int performative = message.getPerformative();
			if (performative == ACLMessage.REQUEST)
			{
				//I cannot answer but I will search for someone who can
				DFAgentDescription dfad = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setName("qwerty");
				dfad.addServices(sd);
				try
				{
					DFAgentDescription[] result = DFService.search(myAgent, dfad);
					if (result.length == 0) myAgent.displayResponse("No service has been found ...");
					else
					{
						String foundAgent = result[0].getName().getLocalName();
						myAgent.displayResponse("Agent " + foundAgent + " is a service provider. Sending message to " + foundAgent);

						String requestId = "" + System.currentTimeMillis();
						myAgent.translationMap.put(requestId, content);
						ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
						forward.addReceiver(new AID(foundAgent, AID.ISLOCALNAME));
						forward.setContent(content);
						forward.setOntology(ontology);
						forward.setReplyWith(requestId);  // Set a unique identifier for the request
						myAgent.send(forward);
					}
				}
				catch (FIPAException ex)
				{
					ex.printStackTrace();
					myAgent.displayResponse("Problem occured while searching for a service ...");
				}
			}
			else
			{	//when it is an answer

				String requestId = message.getInReplyTo();
				String translation = message.getContent();
				String originalWord = myAgent.translationMap.get(requestId);

				myAgent.displayTranslationHtml(originalWord, translation);
				// Remove the entry from the map
				myAgent.translationMap.remove(requestId);

			}
		}
	}

}
