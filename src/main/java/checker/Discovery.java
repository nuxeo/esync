package checker;

import org.reflections.Reflections;

import java.util.Set;

/**
 * Get the checker list
 */
public class Discovery {

    static public Set<Class<? extends AbstractChecker>> getCheckersClass() {
        Reflections reflections = new Reflections("checker");
        Set<Class<? extends AbstractChecker>> checkers =
                reflections.getSubTypesOf(AbstractChecker.class);
        return checkers;
    }

}
