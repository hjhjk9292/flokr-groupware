import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import FacilityReservation from '../../components/facility/FacilityReservation';

const UserFacilityReservation = ({ userData, onLogout }) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [currentPage] = useState('/user/facilities');

  const handleNavigation = (path, params = {}) => {
    if (params.tab) {
      navigate(`${path}?tab=${params.tab}`);
    } else {
      navigate(path);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <main>
        <FacilityReservation 
          userData={userData}
          initialTab={searchParams.get('tab')}
        />
      </main>
    </div>
  );
};

export default UserFacilityReservation;