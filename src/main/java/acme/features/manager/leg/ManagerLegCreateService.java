
package acme.features.manager.leg;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.airports.Airport;
import acme.entities.flights.Flight;
import acme.entities.legs.Leg;
import acme.entities.legs.LegStatus;
import acme.realms.managers.Manager;

@GuiService
public class ManagerLegCreateService extends AbstractGuiService<Manager, Leg> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ManagerLegRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int flightId;
		Flight flight;

		flightId = super.getRequest().getData("masterId", int.class);
		flight = this.repository.findFlightById(flightId);
		status = flight != null && flight.isDraftMode() && super.getRequest().getPrincipal().hasRealm(flight.getManager());

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Leg leg;
		int masterId;
		Flight flight;

		masterId = super.getRequest().getData("masterId", int.class);
		flight = this.repository.findFlightById(masterId);

		leg = new Leg();
		leg.setDraftMode(true);
		leg.setFlight(flight);
		leg.setScheduledArrival(new Date());
		leg.setScheduledDeparture(new Date());

		super.getBuffer().addData(leg);
	}

	@Override
	public void bind(final Leg leg) {
		super.bindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status", "departureAirport", "arrivalAirport", "aircraft");
	}

	@Override
	public void validate(final Leg leg) {
		boolean correctAirportOrder = true;

		List<Leg> legs = this.repository.findAllLegsByFlightId(leg.getFlight().getId());
		if (!legs.isEmpty()) {
			for (Leg thisLeg : legs)
				if (thisLeg == leg) {
					legs.remove(legs.indexOf(thisLeg));
					legs.add(leg);
				}
			legs.sort(Comparator.comparing(Leg::getScheduledDeparture));
			Airport actualAirport = legs.get(0).getDepartureAirport();
			for (Leg actualLeg : legs)
				if (actualLeg.getArrivalAirport().equals(actualAirport) || !actualLeg.getDepartureAirport().equals(actualAirport)) {
					correctAirportOrder = false;
					break;
				} else
					actualAirport = actualLeg.getArrivalAirport();
		}

		super.state(correctAirportOrder, "departureAirport", "manager.leg.update.airportOrder");
	}

	@Override
	public void perform(final Leg leg) {
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		int managerId;
		Manager manager;
		Dataset dataset;
		Collection<Aircraft> aircrafts;
		Collection<Airport> departureAirports;
		Collection<Airport> arrivalAirports;
		SelectChoices aircraftChoices;
		SelectChoices arrivalAirportChoices;
		SelectChoices departureAirportChoices;
		SelectChoices legStatusChoices;

		managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		manager = this.repository.findManagerById(managerId);
		aircrafts = this.repository.findAircraftsByAirlineId(manager.getAirline().getId());
		aircraftChoices = SelectChoices.from(aircrafts, "registrationNumber", leg.getAircraft());

		departureAirports = this.repository.findAllAirports();
		arrivalAirports = this.repository.findAllAirports();
		departureAirportChoices = SelectChoices.from(departureAirports, "name", leg.getDepartureAirport());
		arrivalAirportChoices = SelectChoices.from(arrivalAirports, "name", leg.getArrivalAirport());

		legStatusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());

		dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "draftMode");
		dataset.put("status", legStatusChoices.getSelected().getKey());
		dataset.put("legStatuses", legStatusChoices);
		dataset.put("aircraft", aircraftChoices.getSelected().getKey());
		dataset.put("aircrafts", aircraftChoices);
		dataset.put("departureAirport", departureAirportChoices.getSelected().getKey());
		dataset.put("departureAirports", departureAirportChoices);
		dataset.put("arrivalAirport", arrivalAirportChoices.getSelected().getKey());
		dataset.put("arrivalAirports", arrivalAirportChoices);
		dataset.put("masterId", super.getRequest().getData("masterId", int.class));

		super.getResponse().addData(dataset);
	}

}
