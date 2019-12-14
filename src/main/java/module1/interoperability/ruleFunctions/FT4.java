package module1.interoperability.ruleFunctions;

import module1.interoperability.conector.Conector;

public class FT4 implements FunctionType {
    
    @Override
    public String function(String value,String id, String topic, String unit, String produceValue) {

        try {
            float pV = Float.parseFloat(produceValue);
            float v1 = Float.parseFloat(value);
            if (pV >= v1) {
                return Conector.send(id, topic, unit, produceValue);
            }

            return "";

        } catch (Exception e) {
            return "Error: Produce is not a float value";
        }

    }
    
}