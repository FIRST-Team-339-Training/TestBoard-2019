package frc.Utils;

import frc.Utils.drive.*;
import frc.vision.VisionProcessor;
import frc.Hardware.Hardware;
import frc.HardwareInterfaces.DriveWithCamera.DriveWithCameraState;
import frc.Utils.GamePieceManipulator;
import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 * @author Conner McKevitt
 *
 *         use to deposit a game piece for the 2019 season
 */
public class DepositGamePiece
{

public Drive drive = null;

public Forklift forklift = null;

public GamePieceManipulator gamePieceManipulator = null;

public DepositGamePiece (Drive drive, Forklift forklift,
        GamePieceManipulator gamePieceManipulator)
{
    this.drive = drive;
    this.forklift = forklift;
    this.gamePieceManipulator = gamePieceManipulator;
}



public enum DepositHatchState
    {
    INIT, DEPOSIT_HATCH, BACKUP_HATCH, BACKUP_HATCH_AFTER_FORK, PREP_TO_BACKUP, STOP
    }

public DepositHatchState depositHatchState = DepositHatchState.INIT;

public static boolean hasLoweredAuto = false;

/**
 * Function for use in autonomous that uses a statemachine to
 * deposit a hatch. The forklift must already be at the correct
 * height and the manipulator at the correct angle. To move the
 * manipulator to the correct height, use prepToDeposit. The
 * forklift does not need to be moved up from its starting
 * position in order to deposit a hatch.
 */
public boolean depositHatch (boolean inAuto)
{


    switch (depositHatchState)
        {

        // State machine waits in INIT before before called, and, once
        // called, moves on to the next machine
        case INIT:

            depositHatchState = DepositHatchState.DEPOSIT_HATCH;

            break;

        // Drives the robot forward so the hatch becomes attached
        // to the velcro
        case DEPOSIT_HATCH:
            // TODO magic number for drive speed
            if (this.drive.driveStraightInches(FORWARD_TO_DEPOSIT,
                    .2, BACKUP_ACCELERATION, usingGyro))
                {

                depositHatchState = DepositHatchState.PREP_TO_BACKUP;

                }
            break;

        case PREP_TO_BACKUP:
            if (inAuto)
                {
                if (Hardware.manipulator
                        .moveArmToPosition(DEPOSIT_ARM_ANGLE_AUTO))
                    {
                    depositHatchState = DepositHatchState.BACKUP_HATCH;

                    }
                }
            else
                if (depositHeighthatch == 2)

                    {

                    if (Hardware.manipulator.moveArmToPosition(
                            manipulatorAngle
                                    - 10))
                        {

                        depositHatchState = DepositHatchState.BACKUP_HATCH;

                        }
                    }
                else
                    {
                    if (Hardware.lift.setLiftPosition(
                            forkliftHeight - 3))
                        {
                        depositHatchState = DepositHatchState.BACKUP_HATCH_AFTER_FORK;
                        }
                    }
            break;
        case BACKUP_HATCH:


            if (this.drive.driveStraightInches(BACKUP_INCHES,
                    -BACKUP_SPEED, BACKUP_ACCELERATION,
                    usingGyro))
                {
                depositHatchState = DepositHatchState.STOP;
                }

            break;

        case BACKUP_HATCH_AFTER_FORK:
            if (this.drive.driveStraightInches(BACKUP_INCHES_3,
                    -BACKUP_SPEED_3, BACKUP_ACCELERATION, usingGyro))
                {
                depositHatchState = DepositHatchState.STOP;
                }
            break;
        case STOP:
            this.drive.drive(0, 0);

            depositHatchState = DepositHatchState.INIT;
            return true;
        }
    return false;
}

public enum DepositCargoState
    {
    INIT, RAISE_MANIPULATOR, DEPOSIT_CARGO, BACKUP_CARGO, STOP
    }

public static DepositCargoState depositCargoState = DepositCargoState.INIT;

/**
 * Function to autonomously deposit cargo (for autonomous).
 * The forklift and manipulator must already be set to the
 * proper height and angle.
 *
 * @return true if the function finished, false if it
 *         still needs to be called
 */
public boolean depositCargo ()
{
    switch (depositCargoState)
        {
        case INIT:
            depositCargoState = DepositCargoState.RAISE_MANIPULATOR;
            break;
        case RAISE_MANIPULATOR:
            // if (this.gamePieceManipulator
            // .moveArmToPosition(CARGO_ARM_POSITION))
            // {
            depositCargoState = DepositCargoState.DEPOSIT_CARGO;
            // }
            break;
        case DEPOSIT_CARGO:
            if (this.gamePieceManipulator.spinOutCargoByTimer())
                {
                depositCargoState = DepositCargoState.BACKUP_CARGO;
                }
            break;
        case BACKUP_CARGO:
            if (this.drive.driveStraightInches(BACKUP_INCHES,
                    -BACKUP_SPEED, BACKUP_ACCELERATION,
                    usingGyro))
                {
                depositCargoState = DepositCargoState.STOP;
                }
            break;
        case STOP:
            this.drive.drive(0, 0);

            depositCargoState = DepositCargoState.INIT;
            return true;
        }
    return false;
} // end depositCargo()

private enum DepositTeleopState
    {
    INIT, NOT_DOING_THE_THING, PREP_FOR_ALIGN_FORKLIFT, PREP_FOR_ALIGN_MANIP, PREP_FORKLIFT, PREP_MANIPULATOR, ALIGN_TO_TARGET, LOWER_FORK_1, DRIVE_STRAIGHT_FORWARD, DEPOSIT, FINISH
    }

public DepositTeleopState depositTeleopState = DepositTeleopState.INIT;

public enum DepositHeightHatch
    {
    CARGO_SHIP_HATCH, ROCKET_HATCH_1, ROCKET_HATCH_2, ROCKET_HATCH_3
    }

public enum DepositHeightCargo
    {
    CARGO_SHIP_CARGO, ROCKET_CARGO_1, ROCKET_CARGO_2, ROCKET_CARGO_3
    }



// default is hatch
public boolean hasCargo = false;

public int depositHeighthatch = 0;

public int depositHeightCargo = 0;

public boolean loweringTheFork = false;

public boolean isAllowedToDrive = false;


/**
 * This is the main state machine for depositing the gamepieces in
 * Teleop. To
 * deposit you should call startTeleopDeposit while calling this
 * function in the
 * teleop loop
 *
 */
public boolean depositTeleopStateMachine ()
{
    // System.out.println("Teleop state: " + depositTeleopState);

    switch (depositTeleopState)
        {
        case NOT_DOING_THE_THING:
            break;
        case INIT:

            // if (depositHeighthatch == 1
            // || depositHeightCargo == 1)
            // {
            // // System.out.println("level 2 align first");
            // if (Hardware.driveWithCamera
            // .driveToTargetClose(.1))
            // // || (Hardware.frontUltraSonic
            // // .getDistanceFromNearestBumper() <= 22
            // // && Hardware.rightFrontDriveEncoder
            // // .getDistance() > 10))
            // {
            // Hardware.driveWithCamera.state = DriveWithCameraState.INIT;

            // depositTeleopState = DepositTeleopState.PREP_FOR_ALIGN_MANIP;
            // }
            // }
            // else
            // {
            depositTeleopState = DepositTeleopState.PREP_FOR_ALIGN_MANIP;
            // }
            break;
        case PREP_FOR_ALIGN_MANIP:



            if (moveManipulator(SAFE_MAN_ANGLE))
                depositTeleopState = DepositTeleopState.PREP_FOR_ALIGN_FORKLIFT;
            // if (Hardware.manipulator
            // .moveArmToPosition(
            // SAFE_MAN_ANGLE))
            // {
            // depositTeleopState = DepositTeleopState.PREP_FOR_ALIGN_FORKLIFT;
            // }
            break;
        case PREP_FOR_ALIGN_FORKLIFT:

            if (moveForklift(SAFE_FORKLIFT_HEIGHT))
                depositTeleopState = DepositTeleopState.ALIGN_TO_TARGET;
            // if (Hardware.lift.setLiftPosition(SAFE_FORKLIFT_HEIGHT))
            // {

            // depositTeleopState = DepositTeleopState.ALIGN_TO_TARGET;
            // }
            break;

        case PREP_FORKLIFT:

            // if (hasCargo == false)
            // {
            switch (depositHeighthatch)
                {
                default:
                case 0:
                    forkliftHeight = Forklift.LOWER_ROCKET_HATCH;

                    if (moveForklift(forkliftHeight))
                        depositTeleopState = DepositTeleopState.DEPOSIT;
                    // if (Hardware.lift
                    // .setLiftPosition(
                    // Forklift.LOWER_ROCKET_HATCH))
                    // {

                    // depositTeleopState = DepositTeleopState.DEPOSIT;
                    // }
                    break;

                case 1:
                    // System.out.print("1");
                    forkliftHeight = Forklift.MIDDLE_ROCKET_HATCH;

                    if (moveForklift(forkliftHeight))
                        depositTeleopState = DepositTeleopState.DEPOSIT;
                    // if (Hardware.lift
                    // .setLiftPosition(
                    // Forklift.MIDDLE_ROCKET_HATCH))
                    // {

                    // depositTeleopState = DepositTeleopState.DEPOSIT;
                    // }
                    break;

                case 2:

                    forkliftHeight = Forklift.TOP_ROCKET_HATCH;
                    if (moveForklift(forkliftHeight))
                        depositTeleopState = DepositTeleopState.DEPOSIT;
                    // if (Hardware.lift
                    // .setLiftPosition(Forklift.TOP_ROCKET_HATCH))
                    // {

                    // depositTeleopState = DepositTeleopState.DEPOSIT;
                    // }
                    break;

                case 3:
                    // System.out.print("3");
                    // // forkliftHeight = Forklift.CARGO_SHIP_HATCH;
                    // if (Hardware.lift
                    // .setLiftPosition(Forklift.CARGO_SHIP_HATCH))
                    // {

                    // depositTeleopState = DepositTeleopState.DEPOSIT;
                    // }
                    break;
                }


            break;

        case PREP_MANIPULATOR:

            // if (hasCargo == false)
            // {
            switch (depositHeighthatch)
                {
                default:
                case 0:
                    manipulatorAngle = Forklift.LOWER_ROCKET_HATCH_ANGLE;
                    if (moveManipulator(
                            manipulatorAngle))
                        depositTeleopState = DepositTeleopState.PREP_FORKLIFT;
                    // if (Hardware.manipulator
                    // .moveArmToPosition(
                    // Forklift.LOWER_ROCKET_HATCH_ANGLE))
                    // {
                    // depositTeleopState = DepositTeleopState.PREP_FORKLIFT;
                    // }

                    break;

                case 1:
                    manipulatorAngle = Forklift.MIDDLE_ROCKET_HATCH_ANGLE;
                    if (moveManipulator(
                            manipulatorAngle))
                        depositTeleopState = DepositTeleopState.PREP_FORKLIFT;
                    // if (Hardware.manipulator
                    // .moveArmToPosition(
                    // Forklift.MIDDLE_ROCKET_HATCH_ANGLE))
                    // {
                    // depositTeleopState = DepositTeleopState.PREP_FORKLIFT;
                    // }
                    break;

                case 2:
                    manipulatorAngle = Forklift.TOP_ROCKET_HATCH_ANGLE;
                    if (moveManipulator(
                            manipulatorAngle))
                        depositTeleopState = DepositTeleopState.PREP_FORKLIFT;
                    // if (Hardware.manipulator
                    // .moveArmToPosition(
                    // Forklift.TOP_ROCKET_HATCH_ANGLE))
                    // {
                    // depositTeleopState = DepositTeleopState.PREP_FORKLIFT;
                    // }
                    break;

                case 3:

                    // forkliftHeight = Forklift.CARGO_SHIP_HATCH;
                    break;
                }

            break;
        case ALIGN_TO_TARGET:
            // System.out.println("in vision");
            // if (depositHeighthatch == 1
            // || depositHeightCargo == 1)
            // {
            // depositTeleopState = DepositTeleopState.DEPOSIT;
            // }
            // else

            // TODO uncomment
            if (/*
                 * Hardware.driveWithCamera
                 * .driveToTargetClose(.1)
                 */true)
                {
                Hardware.driveWithCamera.state = DriveWithCameraState.INIT;

                depositTeleopState = DepositTeleopState.PREP_MANIPULATOR;

                // if (depositHeighthatch == 0)
                // {
                // depositTeleopState = DepositTeleopState.LOWER_FORK_1;
                // }
                // depositTeleopState = DepositTeleopState.DEPOSIT;
                }
            break;
        case DRIVE_STRAIGHT_FORWARD:

            // once the drivers get in this state they can start raising the
            // forklift and other stuff

            if (Hardware.drive.driveStraightInches(4, .1, .5,
                    usingGyro))
                {

                }


            // if (Hardware.nextHigherLiftHeightButton
            // .getCurrentValue() == true
            // || Hardware.nextLowerLiftHeightButton
            // .getCurrentValue() == true)
            // {
            // depositTeleopState = DepositTeleopState.FINISH;
            // }
            break;
        case DEPOSIT:

            if (this.depositHatch(false))
                {

                depositTeleopState = DepositTeleopState.FINISH;
                }


            break;
        default:
        case FINISH:
            this.resetDepositTeleop();
            Hardware.drive.resetEncoders();
            return true;
        }

    return false;
}

boolean hasStartedDeposit = false;

/**
 * use to start the deposit in teleop statemachine
 *
 * @param heightLevel
 *                        the level on the rocket that you want to
 *                        deposit to.
 *                        0 = lowest, 1 = middle, 2 = high, 3 = cargo
 *                        ship
 * @param gamepiece
 *                        the type of gamepiece in the manipulator
 *
 */
public boolean startTeleopDeposit (int heightLevel,
        boolean hasCargo)
{

    if (hasCargo == false)
        {
        depositHeighthatch = heightLevel;
        }
    else
        {
        depositHeightCargo = heightLevel;
        }
    if (!hasStartedDeposit)
        {
        // System.out
        // .println("Stop. It's Conner Time");
        hasStartedDeposit = true;
        depositTeleopState = DepositTeleopState.INIT;
        }

    if (depositTeleopState == DepositTeleopState.FINISH)
        {
        hasStartedDeposit = false;
        return true;
        }
    return false;
}


/**
 * Thes set the depositTeleop state machine to it holding state in
 * preparation
 * for depositing
 *
 */
public void resetDepositTeleop ()
{
    Hardware.alignVisionButton.setValue(false);
    hasStartedDeposit = false;
    depositTeleopState = DepositTeleopState.NOT_DOING_THE_THING;


}


private boolean moveManipulator (double angle)
{
    if (Hardware.manipulator.moveArmToPosition(angle))
        {
        return true;
        }
    return false;
}

private boolean moveForklift (double height)
{
    if (Hardware.lift.setLiftPosition(height))
        {
        return true;
        }
    return false;
}

public static boolean hasDoneThePrep = false;

/**
 * function to back up and raise arm to deposit in autonomous. This will
 * only
 * work with the hatch panel
 */
public void prepToDepositHatch ()
{
    if (hasDoneThePrep == false)
        {
        // System.out.println("*Dabs on haters*");
        if (Hardware.manipulator
                .moveArmToPosition(105))
            {
            // System.out.println("*Hater has been dabbed on*");
            hasDoneThePrep = true;

            }
        }
} // end prepToDeposit()

/**
 * This checks to see if any joysticks are being used. To stop the
 * deposit the
 * drivers still have to toggle the button.
 *
 * @return
 */
public boolean overrideVision ()

{
    if (Hardware.leftOperator.getY() > JOYSTICK_DEADBAND
            || Hardware.leftOperator.getY() < -JOYSTICK_DEADBAND
            || Hardware.rightOperator.getY() > JOYSTICK_DEADBAND
            || Hardware.rightOperator
                    .getY() < -JOYSTICK_DEADBAND
            || Hardware.leftDriver.getY() > JOYSTICK_DEADBAND
            || Hardware.leftDriver.getY() < -JOYSTICK_DEADBAND
            || Hardware.rightDriver.getY() > JOYSTICK_DEADBAND
            || Hardware.rightDriver.getY() < -JOYSTICK_DEADBAND
            || Hardware.rightOperator.getRawButton(6) ||
            Hardware.rightOperator.getRawButton(7))
        {
        // System.out.println("Mission Failed. We'll get'em next time");
        depositTeleopState = DepositTeleopState.FINISH;
        return true;
        }

    return false;
}

public void printDebugStatements ()
{

    SmartDashboard.putString("deposit teleop",
            this.depositTeleopState.toString());

    SmartDashboard.putBoolean("hasDoneThePrep",
            hasDoneThePrep);
    SmartDashboard.putBoolean("deposit with vision enabled",
            Hardware.alignVisionButton.isOnCheckNow());

    SmartDashboard.putNumber("forklift height",
            forkliftHeight);

    SmartDashboard.putBoolean("has cargo",
            hasCargo);



    SmartDashboard.putString("deposit hatch state",
            this.depositHatchState.toString());
    // TODO
    // SmartDashboard.putNumber("Blob area",
    // Hardware.axisCamera.getNthSizeBlob(0).area);
}

//
public double forkliftHeight = 0;

public double manipulatorAngle = 0;

// constants for prep

private final double SAFE_FORKLIFT_HEIGHT = 8;// TODO

private final double SAFE_MAN_ANGLE = 20;// TODO

// Hatch constants======================

private static final int FORWARD_TO_DEPOSIT = 4;

private static final double DEPOSIT_ARM_ANGLE_AUTO = 70;

// otro constants===========================

private static final double JOYSTICK_DEADBAND = .2;

private static boolean usingGyro = true;

private static final double BACKUP_INCHES = 10;

private static final double BACKUP_INCHES_3 = 4;

private static final double BACKUP_ACCELERATION = .1;

private static final double BACKUP_SPEED = .2;

private static final double BACKUP_SPEED_3 = .07;

private static final double FORK_SPEED = 1;




}
