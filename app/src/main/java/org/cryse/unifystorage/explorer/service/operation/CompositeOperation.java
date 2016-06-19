package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CompositeOperation extends Operation<CompositeOperation.Params, CompositeOperation.Result> {
    public static final String OP_NAME = "OP_COMPOSITE";

    public CompositeOperation(String token, Params params) {
        super(token, params);
    }

    public CompositeOperation(String token, Params params, OnOperationListener listener, Handler listenerHandler) {
        super(token, params, listener, listenerHandler);
    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    @Override
    protected Result runOperation() {
        Result compositeOperationResult = new Result();
        List<Operation> operations = getParams().getOperations();
        int operationCount = operations.size();
        for (int i = 0; i < operations.size(); i++) {
            Operation operation = operations.get(i);
            try {
                notifyOperationProgress(0, 0, i + 1, operationCount, 0, 0);
                compositeOperationResult.addResult(operation.call());
            } catch (Exception e) {
                compositeOperationResult.addResult(new RemoteOperationResult(e));
                break;
            }
        }
        return compositeOperationResult;
    }

    @Override
    protected void onBuildNotificationForState(OperationState state) {

    }

    @Override
    protected void onBuildNotificationForProgress(long currentRead, long currentSize, long itemIndex, long itemCount, long totalRead, long totalSize) {

    }

    public static class Params extends Operation.Params {
        private ArrayList<Operation> mOperations;

        public Params() {
            mOperations = new ArrayList<>();
        }

        public void addOperation(Operation operation) {
            mOperations.add(operation);
        }

        public List<Operation> getOperations() {
            return mOperations;
        }
    }

    public static class Result extends Operation.Result {
        private ArrayList<Operation.Result> mResults;
        public Result() {
            mResults = new ArrayList<>();
        }

        public void addResult(Operation.Result result) {
            mResults.add(result);
        }

        public Collection<Operation.Result> getResults() {
            return mResults;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public Exception getException() {
            return null;
        }
    }
}
