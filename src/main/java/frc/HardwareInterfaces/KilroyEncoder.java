package frc.HardwareInterfaces;

import com.ctre.phoenix.motorcontrol.can.BaseMotorController;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

/**
 * A sensor class that is able to use both CAN features and DIO features
 * interchangeably. This allows for the hot-swapping of encoders physically,
 * with minimal to no programming changes required.
 *
 * @author Ryan McGee
 *
 */
public class KilroyEncoder implements PIDSource
{
private Encoder dioSensor = null;

private BaseMotorController canSensor = null;

private CANSparkMax canEncoder = null;

private final SensorType type;

/**
 * Create the KilroyEncoder object with a digital i/o encoder. Methods in this
 * class are based on the WPI Encoder class.
 *
 * @param digitalPort1
 * @param digitalPort2
 */
public KilroyEncoder (int digitalPort1, int digitalPort2)
{
    this.dioSensor = new Encoder(digitalPort1, digitalPort2);
    type = SensorType.D_IO;

}

/**
 * Creates the KilroyEncoder object with an encoder attached to a CAN motor
 * controller.
 *
 * @param canMotorController
 *                               The motor controller that the Encoder is
 *                               attached
 *                               to.
 */
public KilroyEncoder (BaseMotorController canMotorController)
{
    this.canSensor = canMotorController;
    type = SensorType.CAN;
}

/**
 * Creats the KilroyEncoder object with an encoder attached to a REV SparkMax
 *
 * @param canMotorController
 */
public KilroyEncoder (CANSparkMax canMotorController)
{
    this.canEncoder = canMotorController;
    type = SensorType.REV_CAN;
    setTicksPerRevolution(1);
}

/**
 * this is the same as above it creats the object AND sets how many ticks we
 * want per revolution
 *
 * @param canMotorController
 * @param ticksPerRevolution
 */
public KilroyEncoder (CANSparkMax canMotorController,
        int ticksPerRevolution)
{
    this.canEncoder = canMotorController;
    type = SensorType.REV_CAN;
    setTicksPerRevolution(ticksPerRevolution);

}

/**
 * Encoders read revolutions / distances by counting a number of pulses based on
 * how much a shaft it is connecting to has rotated. Each pulse is known as a
 * "tick".
 *
 * @return the number of pulses that has taken place since last reset.
 */
public int get ()
{
    switch (type)
        {
        case CAN:
            return canSensor.getSelectedSensorPosition(0);
        case D_IO:
            return dioSensor.get();
        case REV_CAN:
            // Spark Motor Controllers return an double based on the number of
            // rotations it
            // has completed

            // sparkTicksPerRevolution is the number we multi the value of
            // revolutions by to
            // get a larger integer imitating ticks
            if (canEncoder.getInverted() == true)
                return -(int) (this.sparkTicksPerRevolution
                        * canEncoder.getEncoder().getPosition());

            return (int) (this.sparkTicksPerRevolution
                    * canEncoder.getEncoder().getPosition());

        default:
            return 0;
        }
}

/**
 * When we want to measure distance based on the encoder, we multiply by a
 * scalar set earlier (usually during initialization), that translates rotation
 * into linear movement.
 *
 * @return how far the encoder has traveled based on a scalar.
 */
public double getDistance ()
{
    switch (type)
        {
        case CAN:
            return distancePerTick * this.get();
        case D_IO:
            return dioSensor.getDistance();
        case REV_CAN:
            if (canEncoder.getInverted() == true)
                return -distancePerTick
                        * canEncoder.getEncoder().getPosition();
            return distancePerTick
                    * canEncoder.getEncoder().getPosition();
        default:
            return this.get();
        }
}

/**
 * Rate is useful for determining the current speed of the the sensor. It is
 * found by taking the derivative of the position, using past values to figure
 * out the units per second.
 *
 * Useful also for applications such as continuous PID loops.
 *
 * @return How fast the sensor is rotating / moving. It is given in [units] per
 *         second, based on what you input for setDistancePerPulse.
 */
public double getRate ()
{
    switch (type)
        {
        case CAN:
            return (canSensor.getSelectedSensorVelocity(0) * 10)
                    * distancePerTick;
        case D_IO:
            return dioSensor.getRate();

        case REV_CAN:
            if (canEncoder.getInverted() == true)
                return -canEncoder.getEncoder().getVelocity() / 60;
            return canEncoder.getEncoder().getVelocity() / 60;
        default:
            return 0;
        }
}

/**
 * @return What kind of sensor is being used, either CAN or D_IO.
 */
public SensorType getSensorType ()
{
    return this.type;
}

/**
 * @return The CAN device, if being used, that contains the sensor. If digital
 *         is being used, returns null.
 */
public BaseMotorController getAttachedCANDevice ()
{
    return canSensor;
}

/**
 * @return the digital encoder object, as supplied by WPILib. If CAN sensor is
 *         being used, returns null.
 */
public Encoder getAttachedDigitalDevice ()
{
    return dioSensor;
}

/**
 * this returns the current number of ticks for one revoluition of a Spark Max
 */
public int getTicksPerRevolution ()
{
    return this.sparkTicksPerRevolution;
}

/**
 * The Distance per pulse can be found one of two ways: The first (and most
 * precise option) is to calculate it, by using the circumference formula: dpp =
 * (PI * d) / p, where d is the diameter of the wheel, and p is the pulses per
 * revolution. Note that gearboxes will have to be considered as well.
 *
 * The other option is to reset the encoder while the actuator is in a neutral
 * position, and move the actuater a set number of units. Read the number of
 * ticks, and run the formula (x units)/(ticks).
 *
 * @param value
 *                  how far 1 tick is, translated to linear movement (usually
 *                  inches).
 */
public void setDistancePerPulse (double value)
{
    this.distancePerTick = value;
    switch (type)
        {
        case D_IO:
            dioSensor.setDistancePerPulse(value);
            break;

        default:
            return;
        }
}

/**
 * Sets whether or not the sensor is reading backwards. If so, it corrects by
 * returning the reverse of whatever it is receiving.
 *
 * @param inverted
 *                     Whether or not to invert reading of the encoder.
 */
public void setReverseDirection (boolean inverted)
{
    switch (type)
        {
        case CAN:
            canSensor.setSensorPhase(inverted);
            break;
        case D_IO:
            dioSensor.setReverseDirection(inverted);
            break;
        case REV_CAN:
            canEncoder.setInverted(inverted);
            break;
        default:
            return;
        }
}

/**
 * sets the number of ticks per revolution for Spark max
 *
 * @param ticksPerRevolution
 * @return
 */

public int setTicksPerRevolution (int ticksPerRevolution)
{
    return this.sparkTicksPerRevolution = ticksPerRevolution;

}

/**
 * Sets any outstanding/stored values to 0.
 */
public void reset ()
{
    switch (type)
        {
        case CAN:
            canSensor.setSelectedSensorPosition(0, 0, 0);
            break;
        case D_IO:
            dioSensor.reset();
            break;
        case REV_CAN:
            // this dosent work yet but go here when it does
        default:
            return;
        }
}

/**
 * Sets what the current PID source type is: displacement (position), or rate
 * (velocity). This only determines what kind of value the PID loop will
 * receive.
 *
 * @param pidSource
 *                      Either kDisplacement or kRate
 */
@Override
public void setPIDSourceType (PIDSourceType pidSource)
{
    this.sourceType = pidSource;
}

/**
 * @return Either kDisplacement or kRate: whatever was set by the user, or
 *         default kDisplacement.
 */
@Override
public PIDSourceType getPIDSourceType ()
{
    return sourceType;
}

/**
 * @return Either the velocity or position of the sensor, based on what was set
 *         by setPIDSourceType. For use in the PIDController / KilroyPID object.
 */
@Override
public double pidGet ()
{
    switch (sourceType)
        {
        case kDisplacement:
            return this.getDistance();
        case kRate:
            return this.getRate();
        default:
            return 0;
        }
}

/**
 * What kind of sensor is attached.
 *
 * @author Ryan McGee
 *
 */
public enum SensorType
    {
/** Attached to a CAN Motor Controller */
CAN,
/** Attached to the Digital I/O ports */
D_IO,
/** Attached to the REVSparkMax CAN encoder */
REV_CAN
    }

// varialbe at which rotational meausurements of the Spark Max is translated to
// "ticks" (not really ticks)
private int sparkTicksPerRevolution = 1;

private double distancePerTick = 1;

private PIDSourceType sourceType = PIDSourceType.kDisplacement;

}
