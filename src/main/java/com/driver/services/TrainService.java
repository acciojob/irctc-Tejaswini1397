package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        String route = trainEntryDto.getStationRoute().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        Train train=new Train();
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        Train trainSaved=trainRepository.save(train);

        return trainSaved.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        String sourceStation = seatAvailabilityEntryDto.getFromStation().name();
        String destinationStation = seatAvailabilityEntryDto.getToStation().name();
        String route = sourceStation + "," + destinationStation;

        // Get the list of tickets that match the route and trainId
        List<Ticket> bookedTickets = train.getBookedTickets().stream()
                .filter(ticket -> ticket.getTrain().getRoute().contains(route))
                .collect(Collectors.toList());

        // Calculate the total seats available
        int totalSeats = train.getNoOfSeats();
        int bookedSeats = bookedTickets.size();

        int availableSeats = totalSeats - bookedSeats;
        return availableSeats;

    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        // Find the train by trainId
        Train train = trainRepository.findById(trainId).get();

        // Check if the station is present in the route
        String stationName = station.name();
        if (!train.getRoute().contains(stationName)) {
            throw new Exception("Train is not passing from this station");
        }

        // Calculate the number of people boarding at the given station
        int boardingPeopleCount = 0;
        for (Ticket ticket : train.getBookedTickets()) {
            if (ticket.getFromStation().equals(station)) {
                boardingPeopleCount++;
            }
        }

        return boardingPeopleCount;

    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));

        // Initialize the oldest person's age to 0
        int oldestAge = 0;

        // Iterate through the tickets to find the oldest person's age
        for (Ticket ticket : train.getBookedTickets()) {
            List<Passenger>list=ticket.getPassengersList();
            Passenger passenger = list.get(ticket.getTicketId());
            if (passenger != null) {
                int passengerAge = passenger.getAge();
                if (passengerAge > oldestAge) {
                    oldestAge = passengerAge;
                }
            }
        }

        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> passingTrains = new ArrayList<>();

        // Iterate through the list of trains
        for (Train train : trainRepository.findAll()) {
            // Check if the station is present in the train's route
            if (train.getRoute().contains(station.name())) {
                // Check if the departure time lies within the given time frame
                if (isTimeWithinRange(train.getDepartureTime(), startTime, endTime)) {
                    passingTrains.add(train.getTrainId());
                }
            }
        }

        return passingTrains;

    }
    private boolean isTimeWithinRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

}
