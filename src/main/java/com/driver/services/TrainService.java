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
import java.util.Arrays;
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
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).orElse(null);

        if (train == null) {
            // Handle train not found scenario
            return -1;
        }

        // Step 1: Find the total number of seats available on the train
        int totalSeats = train.getNoOfSeats();

        // Step 2: Get the list of booked tickets for the train
        List<Ticket> bookedTickets = train.getBookedTickets();

        // Step 3: Calculate the number of seats booked between boarding and destination stations
        List<String> stations = Arrays.asList(train.getRoute().split(","));
        Station boardingStation = seatAvailabilityEntryDto.getFromStation();
        Station destinationStation = seatAvailabilityEntryDto.getToStation();

        int boardingIndex = stations.indexOf(boardingStation.name());
        int destinationIndex = stations.indexOf(destinationStation.name());

//        if (boardingIndex == -1 || destinationIndex == -1 || boardingIndex >= destinationIndex) {
//            // Invalid stations or boarding station is after or equal to destination station
//            throw new Exception("Invalid stations");
//        }

        int bookedSeatsBetweenStations = 0;

        for (Ticket ticket : bookedTickets) {
            int ticketBoardingIndex = stations.indexOf(ticket.getFromStation());
            int ticketDestinationIndex = stations.indexOf(ticket.getToStation());

            if (ticketBoardingIndex >= boardingIndex && ticketDestinationIndex <= destinationIndex) {
                // Ticket's boarding and destination stations are within the given range
                bookedSeatsBetweenStations++;
            }
        }

        // Step 4: Calculate the available seats count
        int availableSeats = totalSeats - bookedSeatsBetweenStations;

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
        Train train = trainRepository.findById(trainId).get();

        // Initialize the oldest person's age to 0
        int oldestAge = 0;

        // Iterate through the tickets to find the oldest person's age
        for (Ticket ticket : train.getBookedTickets()) {
            Passenger passenger = ticket.getPassengersList().get(trainId);
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
        int count=0;
       List<Integer>lis=new ArrayList<>();
       for(Train train:trainRepository.findAll()){
           if(train.getRoute().contains(station.name())){
               if(train.getDepartureTime().equals(startTime) && train.getDepartureTime().isBefore(endTime)){
                   count++;
                   lis.add(count);
               }
               }
           }
       return lis;
       }

}
