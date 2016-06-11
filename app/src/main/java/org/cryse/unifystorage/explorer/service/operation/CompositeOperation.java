package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CompositeOperation extends Operation<CompositeOperation.CompositeOperationContext, CompositeOperation.CompositeOperationResult> {
    public static final String OP_NAME = "OP_DELETE";
    private boolean mBreakOnError;

    public CompositeOperation(String operationToken, boolean breakOnError) {
        super(operationToken);
        mBreakOnError = breakOnError;
    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    @Override
    protected CompositeOperationResult run(CompositeOperationContext operationContext, OnRemoteOperationListener listener, Handler listenerHandler) {
        CompositeOperationResult compositeOperationResult = new CompositeOperationResult();
        for(Pair<OperationContext, Operation> operationPair : operationContext.getOperations()) {
            compositeOperationResult.addResult(operationPair.second.run(operationPair.first, listener, listenerHandler));
        }
        return compositeOperationResult;
    }

    @Override
    public void run() {

    }

    @Override
    public CompositeOperationResult execute(CompositeOperationContext operationContext, OnRemoteOperationListener listener, Handler listenerHandler) {
        return null;
    }

    @Override
    public Thread executeAsync(CompositeOperationContext operationContext, OnRemoteOperationListener listener, Handler listenerHandler) {
        return null;
    }

    @Override
    public CompositeOperationContext getOperationContext() {
        return null;
    }

    public static class CompositeOperationContext extends Operation.OperationContext {
        private ArrayList<Pair<OperationContext, Operation>> mOperations;

        public CompositeOperationContext() {
            mOperations = new ArrayList<>();
        }

        public void addOperation(OperationContext context, Operation operation) {
            mOperations.add(Pair.create(context, operation));
        }

        public List<Pair<OperationContext, Operation>> getOperations() {
            return mOperations;
        }
    }

    public static class CompositeOperationResult extends Operation.OperationResult {
        private ArrayList<OperationResult> mResults;
        public CompositeOperationResult() {
            mResults = new ArrayList<>();
        }

        public void addResult(OperationResult result) {
            mResults.add(result);
        }

        public Collection<OperationResult> getResults() {
            return mResults;
        }
    }
}
