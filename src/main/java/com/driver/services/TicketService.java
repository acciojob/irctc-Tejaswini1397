package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
       if(train==null){
           throw new Exception("Train is Not Found");
       }
       int totalSeats=train.getNoOfSeats();
       int bookedTickets=train.getBookedTickets().size();
       if(bookedTickets>totalSeats){
           throw new Exception("Less tickets are available");
       }
       String boardingStation=bookTicketEntryDto.getFromStation().name();
       String destinationStation=bookTicketEntryDto.getToStation().name();
       if(!train.getRoute().contains(boardingStation)|| !train.getRoute().contains(destinationStation)){
           throw new Exception("Invalid stations");
       }
        Ticket ticket=new Ticket();
       int fare=train.getNoOfSeats()*ticket.getTotalFare();
       ticket.setTrain(train);
       ticket.setFromStation(Station.valueOf(boardingStation));
       ticket.setToStation(Station.valueOf(destinationStation));
       ticket.setTotalFare(fare);

       Ticket savedTicket=ticketRepository.save(ticket);
       train.getBookedTickets().add(savedTicket);

       Passenger passenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();


        if (passenger != null) {
            passenger.getBookedTickets().add(savedTicket);
            passengerRepository.save(passenger);
        }

        // Step 8: Return Ticket ID
        return savedTicket.getTicketId();
    }
}
