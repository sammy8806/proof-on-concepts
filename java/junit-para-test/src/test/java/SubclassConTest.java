import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Execution(ExecutionMode.SAME_THREAD)
public class SubclassConTest {

    boolean iDone = false;

    boolean pDone = false;

    static Iterable<List<String>> generateI() {
        List<List<String>> retVal = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<String> innerList = new ArrayList<>();
            for (int j = 0; j < 200; j++) {
                innerList.add("i_" + i + "_" + j);
            }
            retVal.add(innerList);
        }
        return retVal;
    }

    static Iterable<List<String>> generateP() {
        List<List<String>> retVal = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<String> innerList = new ArrayList<>();
            for (int j = 0; j < 200; j++) {
                innerList.add("p_" + i + "_" + j);
            }
            retVal.add(innerList);
        }
        return retVal;
    }

    @Nested
    @Execution(ExecutionMode.SAME_THREAD)
    class Import {

        @Nested
        @Execution(ExecutionMode.CONCURRENT)
        class QueryStuff {
            @Test
            void testUntilEverythingDone() throws InterruptedException {
                while (!iDone && !pDone) {
                    System.out.println("[Query] Waiting ...");
                    Thread.sleep(500);
                }
            }
        }

        @Nested
        @Execution(ExecutionMode.CONCURRENT)
        class IStuff {

            @Nested
            @Execution(ExecutionMode.SAME_THREAD)
            class After {
                @Test
                void shouldPublishImpulses() throws InterruptedException {
                    System.out.println("[I:publish] Start");
                    Thread.sleep(5000);
                    iDone = true;
                    System.out.println("[I:publish] End");
                }
            }

            @Nested
            @Execution(ExecutionMode.SAME_THREAD)
            class Do {
                @Execution(ExecutionMode.CONCURRENT)
                @ParameterizedTest
                @MethodSource("SubclassConTest#generateI")
                void shouldGenerateIes(List<String> list) throws InterruptedException {
                    System.out.println("Found I ids #" + list.size());
                    Thread.sleep(5000);
                }
            }

        }

        @Nested
        @Execution(ExecutionMode.SAME_THREAD)
        class PStuff {

            @Nested
            class After {
                @Test
                void shouldPublishPies() throws InterruptedException {
                    System.out.println("[P:publish] Start");
                    Thread.sleep(5000);
                    pDone = true;
                    System.out.println("[P:publish] End");
                }
            }

            @Nested
            class Do {
                @Execution(ExecutionMode.CONCURRENT)
                @ParameterizedTest
                @MethodSource("SubclassConTest#generateP")
                void shouldGenerateP(List<String> list) throws InterruptedException {
                    System.out.println("[P:Batch] Found I ids #" + list.size());
                    Thread.sleep(3000);
                }
            }
        }
    }

    @Nested
    @Execution(ExecutionMode.SAME_THREAD)
    class Pre {
        @Test
        @Order(5)
        void shouldGenerateIGruppe() throws InterruptedException {
            System.out.println("[Pre:GruppenGenerate] Start");
            Thread.sleep(5000);
            System.out.println("[Pre:GruppenGenerate] End");
        }
    }

}
