package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.Operation;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperationResult;

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
    public String getSummaryTitle(Context context) {
        return null;
    }

    @Override
    public String getSummaryContent(Context context) {
        return null;
    }

    @Override
    public String getSimpleSummaryContent(Context context) {
        return null;
    }

    @Override
    public double getSummaryProgress() {
        return 0;
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
