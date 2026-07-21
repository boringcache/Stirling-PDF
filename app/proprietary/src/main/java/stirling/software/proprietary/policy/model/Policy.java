package stirling.software.proprietary.policy.model;

import java.util.List;

/**
 * A stored automation: ordered tool steps, input bindings, and an output destination.
 *
 * <p>Always runnable on demand. Each {@link PipelineInput} references a persisted {@code Source}
 * connection (resolved live at run time) and carries its own optional {@link TriggerConfig}: the
 * trigger decides when that source is pulled, so one input can be watched while another polls, and
 * a {@code null} trigger makes that input manual-only. An input with no trigger, or a policy with
 * no triggered inputs, still runs when the policy is run on demand; a manual run pulls every input.
 */
public record Policy(
        String id,
        String name,
        String owner,
        boolean enabled,
        List<PipelineInput> inputs,
        List<PipelineStep> steps,
        OutputSpec output,
        Long teamId) {

    public Policy {
        inputs = inputs == null ? List.of() : List.copyOf(inputs);
        steps = steps == null ? List.of() : steps;
        output = output == null ? OutputSpec.inline() : output;
    }

    /**
     * Without an explicit owning team. Kept for the engine and tests; the controller always stamps
     * a {@code teamId} on stored policies so they stay scoped to the creating user's team.
     */
    public Policy(
            String id,
            String name,
            String owner,
            boolean enabled,
            List<PipelineInput> inputs,
            List<PipelineStep> steps,
            OutputSpec output) {
        this(id, name, owner, enabled, inputs, steps, output, null);
    }

    /** The source ids this policy pulls from, in input order; a derived view for reads. */
    public List<String> sourceIds() {
        return inputs.stream().map(PipelineInput::sourceId).toList();
    }

    /** The distinct trigger types configured across this policy's inputs (manual inputs aside). */
    public List<String> triggerTypes() {
        return inputs.stream()
                .map(PipelineInput::trigger)
                .filter(trigger -> trigger != null)
                .map(TriggerConfig::type)
                .distinct()
                .toList();
    }

    /** This policy's pipeline as the engine sees it. */
    public PipelineDefinition toDefinition() {
        return new PipelineDefinition(name, steps, output);
    }
}
