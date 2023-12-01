package jadelab1;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.net.*;
import java.io.*;

public class UnifiedServiceAgent extends Agent {
    protected void setup() {
        // Register service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("dictionary-lookup");
        sd.setName("JADE-dictionary-lookup");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new UnifiedDictionaryCyclicBehaviour(this));
    }

    public String makeRequest(String dictionaryName, String word) {
        StringBuffer response = new StringBuffer();
        try {
            URL url;
            URLConnection urlConn;
            DataOutputStream printout;
            DataInputStream input;
            url = new URL("http://dict.org/bin/Dict");
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String content = "Form=Dict1&Strategy=*&Database=" + URLEncoder.encode(dictionaryName) + "&Query=" + URLEncoder.encode(word) + "&submit=Submit+query";
            //forth
            printout = new DataOutputStream(urlConn.getOutputStream());
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            //back
            input = new DataInputStream(urlConn.getInputStream());
            String str;
            while (null != ((str = input.readLine()))) {
                response.append(str);
            }
            input.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        //cut what is unnecessary
        return response.substring(response.indexOf("<hr>") + 4, response.lastIndexOf("<hr>"));
    }
}
class UnifiedDictionaryCyclicBehaviour extends CyclicBehaviour {
    UnifiedServiceAgent agent;

    public UnifiedDictionaryCyclicBehaviour(UnifiedServiceAgent agent) {
        this.agent = agent;
    }

    public void action() {
        ACLMessage message = agent.receive();
        if (message == null) {
            block();
        } else {
            String dictionaryName = message.getOntology();
            String content = message.getContent();
            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            String response = agent.makeRequest(dictionaryName, content);
            reply.setContent(response);
            agent.send(reply);
        }
    }
}
