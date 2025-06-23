
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.bookings.Booking;
import acme.entities.passengers.Passenger;
import acme.realms.customers.Customer;

@Repository
public interface CustomerPassengerRepository extends AbstractRepository {

	@Query("SELECT p FROM Passenger p WHERE p.customer.id = :customerId")
	Collection<Passenger> findPassengersByCustomerId(@Param("customerId") int customerId);

	@Query("SELECT br.passenger FROM BookingRecord br WHERE br.booking.id = :bookingId")
	Collection<Passenger> findPassengersByBookingId(@Param("bookingId") int bookingId);

	@Query("select p from Passenger p where p.id = :masterId")
	Passenger findPassengerById(int masterId);

	@Query("select b from Booking b where b.id = :masterId")
	Booking findBookingById(int masterId);

	@Query("select c from Customer c where c.id = :masterId")
	Customer findCustomerById(int masterId);

	@Query("SELECT p.customer FROM Passenger p WHERE p.id = :passengerId")
	Customer findCustomerByPassengerId(@Param("passengerId") int passengerId);

	@Modifying
	@Query("DELETE FROM BookingRecord br WHERE br.passenger.id = :passengerId")
	void deleteBookingRecordsByPassengerId(@Param("passengerId") int passengerId);

	@Query("SELECT p FROM Passenger p WHERE p.customer.id = :customerId AND p.passportNumber = :passportNumber")
	Passenger findByCustomerIdAndPassportNumber(int customerId, String passportNumber);

}
