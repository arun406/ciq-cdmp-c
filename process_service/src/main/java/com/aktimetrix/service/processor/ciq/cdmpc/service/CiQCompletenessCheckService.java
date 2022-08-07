package com.aktimetrix.service.processor.ciq.cdmpc.service;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.PostProcessor;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.core.service.StepInstanceService;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.Itinerary;
import com.aktimetrix.service.processor.ciq.cdmpc.impl.ItineraryProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
@com.aktimetrix.core.stereotypes.PostProcessor(code = "COMPLETE_CHECKER", processType = "CiQ", processCode = "A2ATRANSPORT")
public class CiQCompletenessCheckService implements PostProcessor {

    @Autowired
    private StepInstanceService stepInstanceService;
    @Autowired
    private ItineraryProvider itineraryProvider;
    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * @param tenant          tenant
     * @param processInstance process instance
     * @return boolean
     */
    private boolean isComplete(String tenant, ProcessInstance processInstance) {
        final Map<String, Object> metadata = processInstance.getMetadata();
        return isComplete(metadata);
    }

    private boolean isComplete(Map<String, Object> metadata) {
        boolean complete;
        String origin = (String) metadata.get("origin");
        String destination = (String) metadata.get("destination");
        int reservationPieces = (int) metadata.getOrDefault("reservationPieces", 0);
        List<Itinerary> itineraries = (List<Itinerary>) metadata.get("itineraries");
        log.debug("itineraries:{}", itineraries);
        complete = isComplete(createDirectedMultigraph(itineraries), origin, destination, reservationPieces);
        log.info(" is the all routes are available : " + complete);
        return complete;
    }

    /**
     * @param g
     * @param origin            origin
     * @param destination       destination
     * @param reservationPieces reservation pieces
     * @return
     */
    private boolean isComplete(DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, String origin, String destination,
                               int reservationPieces) {

        final Set<String> airports = g.vertexSet();
        log.info("all airports in the itineraries");

        for (String airport : airports) {
            log.info("airport : " + airport);
            // get outgoing
            final Set<DefaultWeightedEdge> outgoingEdges = g.outgoingEdgesOf(airport);
            final double outSum = outgoingEdges.stream().mapToDouble(defaultWeightedEdge -> g.getEdgeWeight(defaultWeightedEdge)).sum();
            log.info(String.valueOf(outSum));
            // get incoming
            final Set<DefaultWeightedEdge> inComingEdges = g.incomingEdgesOf(airport);
            final double inSum = inComingEdges.stream().mapToDouble(defaultWeightedEdge -> g.getEdgeWeight(defaultWeightedEdge)).sum();
            log.info(String.valueOf(inSum));

            if (airport.equalsIgnoreCase(origin)) {
                if (outSum != reservationPieces) {
                    return false;
                }
            } else if (airport.equalsIgnoreCase(destination)) {
                if (inSum != reservationPieces) {
                    return false;
                }
            } else {
                if (inSum != outSum) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param itineraries itinerary
     * @return
     */
    private DirectedWeightedMultigraph<String, DefaultWeightedEdge> createDirectedMultigraph(List<Itinerary> itineraries) {
        DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);

        if (itineraries == null || itineraries.isEmpty()) {
            throw new RuntimeException("Unable to build no itineraries found");
        }

        for (Itinerary itinerary : itineraries) {
            String boardPoint = itinerary.getBoardPoint().getCode();
            String offPoint = itinerary.getOffPoint().getCode();
            int pieces = itinerary.getQuantity().getPiece();
            graph.addVertex(boardPoint);
            graph.addVertex(offPoint);
            final DefaultWeightedEdge edge = graph.addEdge(boardPoint, offPoint);
            graph.setEdgeWeight(edge, pieces);
        }
        return graph;
    }

    @Override
    public void postProcess(Context context) {
        final ProcessInstance processInstance = context.getProcessInstance();
        final boolean complete = isComplete(context.getTenant(), processInstance);
        log.debug("Process Status : {}", complete ? "COMPLETE" : "NOTCOMPLETE");
        // update process instance status.
        processInstance.setComplete(complete);
        if (processInstance.isComplete()) {
            processInstance.setActive(true);
        }
        processInstanceService.saveProcessInstance(processInstance);
    }
}