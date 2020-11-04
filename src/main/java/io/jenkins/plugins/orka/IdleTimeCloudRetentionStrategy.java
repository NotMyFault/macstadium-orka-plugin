package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.RetentionStrategy;
import hudson.util.FormValidation;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;


public class IdleTimeCloudRetentionStrategy extends CloudRetentionStrategy {
    private int idleMinutes;
    public static final int RECOMMENDED_MIN_IDLE = 30;
    
    @DataBoundConstructor
    public IdleTimeCloudRetentionStrategy(int idleMinutes) {
        super(normalizeIdleTime(idleMinutes));
        
        this.idleMinutes = normalizeIdleTime(idleMinutes);
    }
    
    static int normalizeIdleTime(int idleMinutes) { 
        return idleMinutes > 0 ? idleMinutes : RECOMMENDED_MIN_IDLE;
    }

    public int getIdleMinutes() {
        return idleMinutes;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Extension
    public static final class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
        @Override
        public String getDisplayName() {
            return "Keep until idle time expires";
        } 
        
        public FormValidation doCheckIdleMinutes(@QueryParameter String value) {
            try {
                int idleMinutesValue = Integer.parseInt(value);
                
                if (0 < idleMinutesValue && idleMinutesValue < RECOMMENDED_MIN_IDLE) {
                    return FormValidation.warning(
                        String.format("Idle timeout less than %d seconds is not recommended.", 
                                RECOMMENDED_MIN_IDLE)
                    );
                }
                
                if (idleMinutesValue <= 0) {
                    return FormValidation.error("Idle timeout must be a positive number.");
                }
                
                return FormValidation.ok();
            } catch (NumberFormatException e) {
                return FormValidation.error("Idle timeout must be a positive number.");
            }
        }
    }

    private Object readResolve() {
        this.idleMinutes = normalizeIdleTime(this.idleMinutes);

        return new IdleTimeCloudRetentionStrategy(idleMinutes);
    }
}
