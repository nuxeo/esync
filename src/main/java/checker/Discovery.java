package checker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Use reflection to get the list of checkers
 */
public class Discovery {

    static public Set<Class<? extends AbstractChecker>> getCheckersClass(
            List<String> filter) {
        Reflections reflections = new Reflections("checker");
        Set<Class<? extends AbstractChecker>> checkers = reflections
                .getSubTypesOf(AbstractChecker.class);
        if (filter == null || filter.isEmpty()) {
            return checkers;
        }
        Set<Class<? extends AbstractChecker>> ret = new HashSet<>(filter.size());
        for (Class klass : checkers) {
            if (filter.contains(klass.getSimpleName())) {
                ret.add(klass);
            }
        }
        return ret;
    }

}
