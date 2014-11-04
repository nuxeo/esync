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
            Set<String> filter, Set<String> blackList) {
        Reflections reflections = new Reflections("checker");
        Set<Class<? extends AbstractChecker>> checkers = reflections
                .getSubTypesOf(AbstractChecker.class);
        if ((filter == null || filter.isEmpty()) && (blackList == null || blackList.isEmpty())) {
            return checkers;
        }
        Set<Class<? extends AbstractChecker>> ret = new HashSet<>(filter.size());
        // explicit list
        if (filter != null && !filter.isEmpty()) {
            for (Class klass : checkers) {
                if (filter.contains(klass.getSimpleName())) {
                    ret.add(klass);
                }
            }
            return ret;
        }
        // all except black listed
        for (Class klass : checkers) {
            String name = klass.getSimpleName();
            if (blackList.contains(name)) {
                continue;
            }
            ret.add(klass);
        }
        return ret;
    }

}
