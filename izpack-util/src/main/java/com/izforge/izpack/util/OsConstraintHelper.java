package com.izforge.izpack.util;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates OS constraints specified on creation time and allows to check them against the
 * current OS.
 * <p/>
 * For example, this is used for &lt;executable&gt;s to check whether the executable is suitable for
 * the current OS.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class OsConstraintHelper
{
    /**
     * Extract a list of OS constraints from given element.
     *
     * @param element parent IXMLElement
     * @return List of OsModel (or empty List if no constraints found)
     */
    public static List<OsModel> getOsList(IXMLElement element)
    {
        // get os info on this executable
        ArrayList<OsModel> osList = new ArrayList<OsModel>();
        for (IXMLElement osElement : element.getChildrenNamed("os"))
        {
            osList.add(
                    new OsModel(
                            osElement.getAttribute("arch",
                                                   null),
                            osElement.getAttribute("family",
                                                   null),
                            osElement.getAttribute("jre",
                                                   null),
                            osElement.getAttribute("name",
                                                   null),
                            osElement.getAttribute("version",
                                                   null))
            );
        }
        // backward compatibility: still support os attribute
        String osattr = element.getAttribute("os");
        if ((osattr != null) && (osattr.length() > 0))
        {
            // add the "os" attribute as a family constraint
            osList.add(
                    new OsModel(null, osattr, null, null, null)
            );
        }

        return osList;
    }
    
    public static class UnsatisfiableOsConstraintsException extends Exception
    {
        private static final String MESSAGE = "Common OS constraints of %s and %s are unsatisfiable";
        
        public UnsatisfiableOsConstraintsException(List<OsModel> osList, List<OsModel> otherOsList)
        {
            super(String.format(MESSAGE,
                    toOsContraintsString(osList), toOsContraintsString(otherOsList)));
        }
        
        private static String toOsContraintsString(List<OsModel> osList) {
            if (osList.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            sb.append(osList.get(0));
            for (int i = 1; i < osList.size(); i++) {
                sb.append(" or ");
                sb.append(osList.get(i));
            }
            sb.append(" ]");
            return sb.toString();
        }
    }
    
    /**
     * Computes a list of constraints which satisfies both {@code osList} and
     * {@code otherOsList}. If the combination of {@code osList} and
     * {@code otherOsList} is unsatisfiable, a respective exception is thrown.
     * 
     * @param osList list of OS constraints
     * @param otherOsList other list of OS constraints
     * @return list of {@link OsModel}-s which satisfies both {@code osList} and
     * {@code otherOsList}
     * @throws UnsatisfiableOsConstraintsException when both {@code osList} and
     * {@code otherOsList} cannot be satisfied
     */
    public static List<OsModel> commonOsList(List<OsModel> osList, List<OsModel> otherOsList)
            throws UnsatisfiableOsConstraintsException
    {
        // handle trivial cases, one or both lists contain no constraints
        if (osList.isEmpty() && otherOsList.isEmpty())
        {
            return Collections.emptyList();
        }
        else if (osList.isEmpty())
        {
            return otherOsList;
        }
        else if (otherOsList.isEmpty())
        {
            return osList;
        }
        
        List<OsModel> commonOsList = new ArrayList<OsModel>();
        for (OsModel os : osList)
        {
            for (OsModel otherOs : otherOsList)
            {
                CommonOsConstraint arch = OsConstraintHelper.commonConstraint(os.getArch(), otherOs.getArch());
                CommonOsConstraint family = OsConstraintHelper.commonConstraint(os.getFamily(), otherOs.getFamily());
                CommonOsConstraint jre = OsConstraintHelper.commonConstraint(os.getJre(), otherOs.getJre());
                CommonOsConstraint name = OsConstraintHelper.commonConstraint(os.getName(), otherOs.getName());
                CommonOsConstraint version = OsConstraintHelper.commonConstraint(os.getVersion(), otherOs.getVersion());
                
                if (arch.satisfiable
                        && family.satisfiable
                        && jre.satisfiable
                        && name.satisfiable
                        && version.satisfiable)
                {
                    commonOsList.add(new OsModel(
                            arch.value, family.value, jre.value, name.value, version.value));
                }
                
            }
        }
        
        if (!commonOsList.isEmpty())
        {
            return commonOsList;
        }
        else
        {
            throw new UnsatisfiableOsConstraintsException(osList, otherOsList);
        }
    }
    
    /**
     * Creates a common constraint which satisfies both {@code value} and
     * {@code otherValue}.
     * 
     * @param value contraint's value
     * @param otherValue other constraint's value
     * @return common constraint, may return unsatisfiable constraint
     */
    private static CommonOsConstraint commonConstraint(String value, String otherValue) {
        if (value == null && otherValue == null)
        {
            return CommonOsConstraint.ANY;
        }
        else if (value == null)
        {
            return new CommonOsConstraint(otherValue);
        }
        else if (otherValue == null)
        {
            return new CommonOsConstraint(value);
        }
        else if (value.equals(otherValue))
        {
            return new CommonOsConstraint(value);
        }
        else
        {
            return CommonOsConstraint.NONE;
        }
    }
    
    /**
     * Helper common OS constraint result type.
     */
    private static class CommonOsConstraint
    {
        static final CommonOsConstraint ANY = new CommonOsConstraint(true);
        static final CommonOsConstraint NONE = new CommonOsConstraint(false);
        
        final boolean satisfiable;
        final String value;

        CommonOsConstraint(boolean satisfiable)
        {
            this.satisfiable = satisfiable;
            this.value = null;
        }
        
        CommonOsConstraint(String value)
        {
            this.satisfiable = true;
            this.value = value;
        }
    }
}
