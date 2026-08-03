package com.smartbudget.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * TICKET-F015 — SavingsGoal POJO (Day 2)
 *
 * Java mirror of the Day 1 `savings_goals` table plus two derived methods
 * ({@link #getProgressPercentage()} and {@link #isCompleted()}) that Day 6's REST
 * endpoint and Day 9's React progress bar both call.
 *
 * IMPORTANT: {@link BigDecimal#divide(BigDecimal)} throws {@code ArithmeticException}
 * on any non-terminating decimal (e.g. 1/3). Always pass a scale + rounding mode.
 */
public class SavingsGoal {

    private int goalId;
    private int userId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;

    public SavingsGoal() { }

    public SavingsGoal(int goalId, int userId, String goalName,
                       BigDecimal targetAmount, BigDecimal currentAmount,
                       LocalDate deadline) {
        this.goalId        = goalId;
        this.userId        = userId;
        this.goalName      = goalName;
        this.targetAmount  = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline      = deadline;
    }

    public int getGoalId()                            { return goalId; }
    public void setGoalId(int goalId)                 { this.goalId = goalId; }

    public int getUserId()                            { return userId; }
    public void setUserId(int userId)                 { this.userId = userId; }

    public String getGoalName()                       { return goalName; }
    public void setGoalName(String goalName)          { this.goalName = goalName; }

    public BigDecimal getTargetAmount()               { return targetAmount; }
    public void setTargetAmount(BigDecimal target)    { this.targetAmount = target; }

    public BigDecimal getCurrentAmount()              { return currentAmount; }
    public void setCurrentAmount(BigDecimal current)  { this.currentAmount = current; }

    public LocalDate getDeadline()                    { return deadline; }
    public void setDeadline(LocalDate deadline)       { this.deadline = deadline; }

    /**
     * Returns {@code (currentAmount / targetAmount) * 100} as a {@code BigDecimal}
     * with 4 decimal places, rounded HALF_UP. Returns 0 when the target is null or 0
     * (avoids divide-by-zero).
     */
    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (currentAmount == null) {
            return BigDecimal.ZERO;
        }
        return currentAmount
                .divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /** True when currentAmount >= targetAmount. */
    public boolean isCompleted() {
        return currentAmount != null
            && targetAmount  != null
            && currentAmount.compareTo(targetAmount) >= 0;
    }

    @Override
    public String toString() {
        return String.format(
            "Goal[%s, %.2f / %.2f (%.1f%%), due %s]",
            goalName, currentAmount, targetAmount,
            getProgressPercentage(), deadline);
    }
}
