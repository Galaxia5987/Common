package frc.robot;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.swerve.SwerveDrive;
import frc.robot.vision.Vision;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.AutoLogOutput;

public class PoseEstimation {
    private static PoseEstimation INSTANCE = null;
    private final SwerveDrivePoseEstimator estimator;
    private final Vision vision = Vision.getInstance();
    private final SwerveDrive swerveDrive = SwerveDrive.getInstance();

    public PoseEstimation() {
        estimator =
                new SwerveDrivePoseEstimator(
                        swerveDrive.getKinematics(),
                        swerveDrive.getYaw(),
                        swerveDrive.getModulePositions(),
                        swerveDrive.getBotPose());
    }

    public static PoseEstimation getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PoseEstimation();
        }
        return INSTANCE;
    }

    public void addVisionMeasurement(
            DoubleSupplier std1, DoubleSupplier std2, DoubleSupplier std3) {
        var results = vision.getResults();
        for (org.photonvision.EstimatedRobotPose result : results) {
            estimator.addVisionMeasurement(
                    result.estimatedPose.toPose2d(),
                    result.timestampSeconds,
                    VecBuilder.fill(std1.getAsDouble(), std2.getAsDouble(), std3.getAsDouble()));
        }
    }

    public void updatePose() {
        for (int i = 0; i < swerveDrive.getHighFreqModulePositions().size(); i++) {
            estimator.updateWithTime(
                    swerveDrive.getHighFreqTimeStamps()[i],
                    swerveDrive.getYaw(),
                    swerveDrive.getHighFreqModulePositions().get(i));
        }
    }

    public void resetPose(Pose2d pose) {
        estimator.resetPosition(
                swerveDrive.getRawYaw(),
                swerveDrive
                        .getHighFreqModulePositions()
                        .get(swerveDrive.getHighFreqModulePositions().size() - 1),
                pose);
    }

    @AutoLogOutput(key = "EstimatedRobotPose")
    public Pose2d getEstimatedPose() {
        return estimator.getEstimatedPosition();
    }
}
