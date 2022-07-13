package it.polito.tdp.rivers.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.rivers.db.RiversDAO;

public class Model {

	List<River> rivers;
	//gli eventi sono i flow
	private PriorityQueue<Flow> queue;
	
	public Model() {
		RiversDAO dao = new RiversDAO();
		rivers = dao.getAllRivers();
		for(River r: rivers) {
			dao.getFlows(r);
		}
	}

	public List<River> getRivers() {
		return rivers;
	}
	
	public LocalDate getStartDate(River r) {
		if(!r.getFlows().isEmpty()) {
			return r.getFlows().get(0).getDay();
		}
		return null;
	}
	
	public LocalDate getEndDate(River r) {
		if(!r.getFlows().isEmpty()) {
			return r.getFlows().get(r.getFlows().size()-1).getDay();
		}
		return null;
	}
	
	public int getNumMeasurements(River r) {
		return r.getFlows().size();
	}
	
	public double getFMed(River r) {
		double avg=0;
		for(Flow f: r.getFlows()) {
			avg+=f.getFlow();
		}
		avg/=r.getFlows().size();
		r.setFlowAvg(avg);
		return avg;
	}
	
	public Simulatore sim(River r, double k) {
		this.queue = new PriorityQueue<Flow>();
		this.queue.addAll(r.getFlows());
		
		List<Double> capacity = new ArrayList<Double>();
		
		double Q=k*30*courtM3SecToM3Day(r.getFlowAvg());
		double C=Q/2;
		double fOutMin=courtM3SecToM3Day(0.8*r.getFlowAvg());
		int numberOfDays=0;
		
		System.out.println("Q = "+Q);
		
		Flow flow;
		while((flow=this.queue.poll())!=null) {
			double fOut = fOutMin;
			
			if(Math.random()>0.95) {
				fOut=10*fOutMin;
			}
			
			C+=courtM3SecToM3Day(flow.getFlow());
			
			if(C>Q) {
				C=Q;
			}
			
			if(C<fOut) {
				numberOfDays+=1;
				C=0;
			}else {
				C-=fOut;
			}
			
			capacity.add(C);
		}
		
		double CAvg=0;
		for(Double d: capacity) {
			CAvg+=d;
		}
		
		CAvg/=capacity.size();
		return new Simulatore(CAvg, numberOfDays);
	}

	private double courtM3DayToM3Sec(double flow) {
		return flow/60/60/24;
	}

	private double courtM3SecToM3Day(double flow) {
		return flow*60*60*24;
	}

	public void setRivers(List<River> rivers) {
		this.rivers = rivers;
	}

	public PriorityQueue<Flow> getQueue() {
		return queue;
	}

	public void setQueue(PriorityQueue<Flow> queue) {
		this.queue = queue;
	}
	
	
	
}
