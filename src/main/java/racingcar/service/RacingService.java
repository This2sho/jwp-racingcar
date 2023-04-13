package racingcar.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import racingcar.dao.RacingDao;
import racingcar.dao.dto.CarDto;
import racingcar.dao.dto.TrackDto;
import racingcar.model.car.Car;
import racingcar.model.car.Cars;
import racingcar.model.car.strategy.MovingStrategy;
import racingcar.model.car.strategy.RandomMovingStrategy;
import racingcar.model.track.Track;

import java.util.List;

@Service
public class RacingService {

    private final RacingDao racingDao;
    private final MovingStrategy movingStrategy = new RandomMovingStrategy();

    public RacingService(final RacingDao racingDao) {
        this.racingDao = racingDao;
    }

    @Transactional
    public Cars play(final String names, final String trialTimes) {
        final Cars cars = makeCars(names, movingStrategy);
        final Track track = makeTrack(cars, trialTimes);
        final Cars finishedCars = startRace(track);

        final int trackId = saveTrack(track);
        saveCars(trackId, finishedCars);

        return finishedCars;
    }

    private void saveCars(final int trackId, final Cars finishedCars) {
        final List<Car> winnerCars = finishedCars.getWinnerCars();
        final List<Car> carsCurrentInfo = finishedCars.getCarsCurrentInfo();

        for (Car car : carsCurrentInfo) {
            racingDao.save(new CarDto(car.getCarName(), car.getPosition(), winnerCars.contains(car), trackId));
        }
    }

    private int saveTrack(final Track track) {
        return racingDao.save(new TrackDto(track.getTrialTimes()));
    }

    private Cars makeCars(String name, final MovingStrategy movingStrategy) {
        return new Cars(name, movingStrategy);
    }

    private Track makeTrack(final Cars cars, String trialTimes) {
        return new Track(cars, trialTimes);
    }

    public Cars startRace(final Track track) {
        while (track.runnable()) {
            track.race();
        }

        return track.getCars();
    }
}
