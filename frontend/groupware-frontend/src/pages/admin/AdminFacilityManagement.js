import React from 'react';
import FacilityManagement from '../../components/facility/FacilityManagement';

const AdminFacilityManagement = ({ userData, onLogout }) => {
  return (
    <div className="min-h-screen bg-gray-50">
      <FacilityManagement />
    </div>
  );
};

export default AdminFacilityManagement;